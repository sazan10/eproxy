package com.eaio.eproxy.api

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.*
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.*
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.ContentType
import org.apache.http.entity.InputStreamEntity
import org.apache.http.message.BasicHeaderValueParser
import org.apache.http.message.ParserCursor
import org.apache.http.util.CharArrayBuffer
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.*
import com.eaio.eproxy.rewriting.html.*
import com.eaio.io.RangeInputStream
import com.eaio.net.httpclient.AbortHttpUriRequestTask
import com.eaio.net.httpclient.ReEncoding
import com.eaio.net.httpclient.TimingInterceptor
import com.google.apphosting.api.DeadlineExceededException

/**
 * Proxies and optionally rewrites content.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RestController
@Slf4j
class Proxy implements URLManipulation {
    
    // Note: Does not support multiple byte-range-sets.
    @Lazy
    private Pattern byte_ranges_specifier_1 = ~/(?i)^bytes=(\d+)-(\d*)$/, byte_ranges_specifier_2 = ~/(?i)^bytes=-(\d+)$/
    
    @Value('${http.totalTimeout}')
    Long totalTimeout
    
    @Autowired
    HttpClient httpClient
    
    @Autowired
    Rewriting rewriting
    
    @Autowired
    ReEncoding reEncoding
    
    @Autowired(required = false)
    Timer timer

    @RequestMapping('/{scheme:https?}/**')
    void proxy(@PathVariable String scheme, HttpServletRequest request, HttpServletResponse response) {
        proxy(null, scheme, request, response)
    }
    
    @RequestMapping('/{rewriteConfig}-{scheme:https?}/**')
    void proxy(@PathVariable('rewriteConfig') String rewriteConfigString, @PathVariable('scheme') String scheme, HttpServletRequest request, HttpServletResponse response) {
        URI baseURI = buildBaseURI(request.scheme, request.serverName, request.serverPort, request.contextPath)
        URI requestURI = buildRequestURI(scheme, stripContextPathFromRequestURI(request.contextPath, request.requestURI), request.queryString)
        if (!requestURI.host) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
            return
        }

        HttpCacheContext context = HttpCacheContext.create()
        HttpResponse remoteResponse
        try {
            HttpUriRequest uriRequest = newRequest(request.method, requestURI)
            addRequestHeaders(request, uriRequest)
            if (uriRequest instanceof HttpEntityEnclosingRequest) {
                setRequestEntity(uriRequest, request.getHeader('Content-Length'), request.inputStream)
            }
            
            if (totalTimeout) {
                timer?.schedule(new AbortHttpUriRequestTask(uriRequest), totalTimeout)
            }
            
            remoteResponse = httpClient.execute(uriRequest, context)
            
            response.status = remoteResponse.statusLine.statusCode
            
            ContentType contentType = ContentType.getLenient(remoteResponse.entity)
            OutputStream outputStream = response.outputStream
            HeaderElement contentDisposition = parseContentDispositionValue(request.getHeader('Content-Disposition'))
            
            RewriteConfig rewriteConfig = rewriteConfigString ? RewriteConfig.fromString('rnw') : null
            
            boolean canRewrite = rewriting.canRewrite(contentDisposition, rewriteConfig, contentType?.mimeType)
            
            remoteResponse.headerIterator().each { Header header ->
                if (header.name?.equalsIgnoreCase('Location')) { // TODO: Link and Refresh:, CORS headers ...
                    response.setHeader(header.name, rewrite(baseURI, requestURI, header.value, rewriteConfig))
                }
                else if (!dropHeader(header.name, canRewrite)) {
                    // TODO only drop Content-Length if not rewriting
                    response.setHeader(header.name, header.value)
                }
            }
            
            if (remoteResponse.entity) {
                
                long contentLength = remoteResponse.entity.contentLength
                List<Long> range
                if (contentLength >= 0L) {
                    try {
                        range = parseRange(contentLength, request.getHeader('Range'))
                    }
                    catch (IllegalArgumentException ex) {
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                        response.setHeader('Content-Range', "bytes 0-${contentLength - 1}/${contentLength}")
                        return
                    }
                }
                
                if (canRewrite) {
                    if (range) {
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                    }
                    else {
                        rewriting.rewrite(remoteResponse.entity.content, outputStream, contentType.charset, baseURI, requestURI, rewriteConfig, contentType.mimeType)
                    }
                }
                else {
                    response.setHeader('Accept-Ranges', 'bytes')
                    if (range) {
                        response.status = HttpServletResponse.SC_PARTIAL_CONTENT
                        response.setHeader('Content-Range', "bytes ${range[0]}-${range[1] - 1}/${contentLength}")
                    }
                    IOUtils.copyLarge(createRangeStream(remoteResponse.entity.content, range), outputStream) // Do not use HttpEntity#writeTo(OutputStream) -- doesn't get counted in all instances.
                }
            }
                        
            TimingInterceptor.log(context, log)
        }
        catch (IllegalStateException ignored) {}
        catch (SocketException ex) {
            if (ex.message?.startsWith('Permission denied')) { // Google App Engine
                sendError(requestURI, response, HttpServletResponse.SC_FORBIDDEN, ex)
            }
            else if (ex.message?.contains('Resource temporarily unavailable')) { // Google App Engine
                sendError(requestURI, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, ex)
            }
            else if (ex.message?.contains('connection reset by peer')) { // Google App Engine+
                sendError(requestURI, response, HttpServletResponse.SC_NOT_FOUND, ex)
            }
            else {
                throw ex
            }
        }
        catch (SSLException ex) {
            if (ex instanceof SSLHandshakeException) {
                sendError(requestURI, response, HttpServletResponse.SC_NOT_FOUND, ex)
            }
            else if ((ExceptionUtils.getRootCause(ex) ?: ex).message == 'Prime size must be multiple of 64, and can only range from 512 to 1024 (inclusive)') {
                sendError(requestURI, response, HttpServletResponse.SC_FORBIDDEN, ex, "Please upgrade to Java 8. ${requestURI.host} uses more than 1024 Bits in their public key.")
            }
            else {
                throw ex
            }
        }
        catch (IOException ex) {
            if (ex instanceof NoHttpResponseException || ex instanceof SocketTimeoutException || ex instanceof ConnectTimeoutException ||
                ex instanceof UnknownHostException || ex instanceof HttpHostConnectException || ex instanceof ClientProtocolException) {
                sendError(requestURI, response, HttpServletResponse.SC_NOT_FOUND, ex)
            }
            else if (ex.message == 'Connection reset by peer') {
                sendError(requestURI, response, HttpServletResponse.SC_NOT_FOUND, ex)
            }
            else if (ex.message != 'Broken pipe') {
                throw ex
            }
        }
        catch (DeadlineExceededException ex) {
            sendError(requestURI, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, ex)
        }
        finally {
            EntityUtils.consumeQuietly(remoteResponse?.entity)
        }
    }
    
    private void sendError(URI requestURI, HttpServletResponse response, int statusCode, Throwable thrw, String message = (ExceptionUtils.getRootCause(thrw) ?: thrw).message) {
        try {
            response.sendError(statusCode, message)
        }
        catch (IllegalStateException ex) {}
    }
    
    @CompileStatic
    private HttpUriRequest newRequest(String method, URI uri) {
        switch (method) {
            case 'GET': return new HttpGet(uri)
            case 'DELETE': return new HttpDelete(uri)
            case 'HEAD': return new HttpHead(uri)
            case 'OPTIONS': return new HttpOptions(uri)
            case 'PATCH': return new HttpPatch(uri)
            case 'POST': return new HttpPost(uri)
            case 'PUT': return new HttpPut(uri)
            case 'TRACE': return new HttpTrace(uri)
        }
    }
    
    URI buildBaseURI(String scheme, String host, int port, String contextPath) {
        new URI(scheme, null, host, getPort(scheme, port), contextPath, null, null)
    }
    
    /**
     * Make sure to remove the context path before calling this method.
     */
    @CompileStatic
    URI buildRequestURI(String scheme, String requestURI, String queryString) {
        String uriFromHost = substringAfter(requestURI[1..-1], '/'), path = substringAfter(uriFromHost, '/') ?: '/',
            hostAndPort = substringBefore(uriFromHost, '/'), host = hostAndPort, port
        if (hostAndPort.contains(':')) {
            host = substringBefore(hostAndPort, ':')
            port = substringAfter(hostAndPort, ':')
        }
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme(scheme).host(host).path(path)
        if (port) {
            builder.port(port)
        }
        if (queryString) {
            reEncoding.reEncode(builder.build().toUriString() + '?' + queryString).toURI()
        }
        else {
            reEncoding.reEncode(builder.build() as String).toURI()
        }
    }
    
    /**
     * Removes the context path prefix from <tt>requestURI</tt>.
     */
    String stripContextPathFromRequestURI(String contextPath, String requestURI) {
        contextPath ? substringAfter(requestURI, contextPath) : requestURI
    }
    
    int getPort(String scheme, int port) {
        port == -1I || (scheme == 'http' && port == 80I) || (scheme == 'https' && port == 443I) ? -1I : port
    }
    
    void setRequestEntity(HttpEntityEnclosingRequest uriRequest, String contentLength, InputStream inputStream) {
        uriRequest.entity = new InputStreamEntity(inputStream, contentLength?.isLong() ? contentLength as long : -1L)
    }
    
    void addRequestHeaders(HttpServletRequest request, HttpUriRequest uriRequest) {
        [ 'Accept', 'Accept-Language' ].each {
            if (request.getHeader(it)) {
                uriRequest.setHeader(it, request.getHeader(it))
            }
        }
    }
    
    HeaderElement parseContentDispositionValue(String contentDisposition) {
        if (contentDisposition) {
            CharArrayBuffer buf = new CharArrayBuffer(contentDisposition.length())
            buf.append(contentDisposition)
            ParserCursor cursor = new ParserCursor(0I, contentDisposition.length())
            HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(buf, cursor)
            elements[0I]
        }
    }

    /**
     * Returns whether a certain header (ignoring case) should be dropped. Drops <tt>Content-Length</tt> if rewriting.
     */
    // TODO Header whitelist
    boolean dropHeader(String name, boolean canRewrite) {
        (canRewrite ?
            [ 'Content-Security-Policy', 'Transfer-Encoding', 'Accept-Ranges', 'Date', 'Pragma', 'Content-Length' ] :
            [ 'Content-Security-Policy', 'Transfer-Encoding', 'Accept-Ranges', 'Date', 'Pragma' ]
         ).any { it.equalsIgnoreCase(name) }
    }
    
    /**
     * Returns start and end offsets from <tt>Range</tt> request headers.
     *
     * @param contentLength the length of the entity in octets
     * @param range the <tt>Range</tt> header, may be <code>null</code>
     * @return a list of longs (start and end offset) or <code>null</code>
     * @throws IllegalArgumentException on malformed range requests
     */
    private List<Long> parseRange(long contentLength, String range) {
        Matcher m
        if ((m = byte_ranges_specifier_1.matcher(range ?: '')).matches()) {
            long start = m.group(1) as long
            long end = m.group(2) ? m.group(2) as long : contentLength
            
            if (m.group(2)) {
                long temp = start
                start = Math.min(start, end)
                end = Math.max(temp, end)
            }
            else {
                start = Math.min(start, contentLength)
            }
                        
            if (end > contentLength || start == end) {
                throw new IllegalArgumentException("${range} does not match file length ${contentLength}")
            }
            
            start = Math.min(start, contentLength)
            end = Math.min(end + 1L, contentLength)
            [ start, end ]
        }
        else if ((m = byte_ranges_specifier_2.matcher(range ?: '')).matches()) {
            long end = Math.min((m.group(1) as long) + 1L, contentLength)
            [ 0L, end ]
        }
    }
    
    private InputStream createRangeStream(InputStream stream, List<Long> range) throws IOException {
        range ? new RangeInputStream(stream, range.get(0), range.get(1)) : stream 
    }
    
}
