package com.eaio.eproxy.api

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.*
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
@Mixin(URLManipulation)
@RestController
@Slf4j
class Proxy {
    
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
    void proxy(@PathVariable('rewriteConfig') String rewriteConfig, @PathVariable('scheme') String scheme, HttpServletRequest request, HttpServletResponse response) {
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
            
            response.setStatus(remoteResponse.statusLine.statusCode)
            
            remoteResponse.headerIterator().each { Header header ->
                if (header.name?.equalsIgnoreCase('Location')) { // TODO: Link and Refresh:, CORS headers ...
                    response.setHeader(header.name, rewrite(baseURI, requestURI, header.value, rewriteConfig ? new RewriteConfig(rewrite: true) : null))
                }
                else if (!dropHeader(header.name)) {
                    // TODO only drop Content-Length if not rewriting
                    response.setHeader(header.name, header.value)
                }
            }
            
            if (remoteResponse.entity) {
                ContentType contentType = ContentType.getLenient(remoteResponse.entity)
                OutputStream outputStream = response.outputStream
                HeaderElement contentDisposition = parseContentDispositionValue(request.getHeader('Content-Disposition'))
                if (rewriting.canRewrite(contentDisposition, rewriteConfig ? new RewriteConfig(rewrite: true) : null, contentType?.mimeType)) {
                    rewriting.rewrite(remoteResponse.entity.content, outputStream, contentType.charset, baseURI, requestURI, new RewriteConfig(rewrite: true), contentType.mimeType)
                }
                else {
                    IOUtils.copyLarge(remoteResponse.entity.content, outputStream) // Do not use HttpEntity#writeTo(OutputStream) -- doesn't get counted in all instances.
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
                ex instanceof UnknownHostException || ex instanceof HttpHostConnectException) {
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
        String uriFromHost, path
        uriFromHost = substringAfter(requestURI[1..-1], '/')
        path = substringAfter(uriFromHost, '/') ?: '/'
        // TODO: Support for Ports
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme(scheme).host(substringBefore(uriFromHost, '/')).path(path)
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

    // TODO Header whitelist
    boolean dropHeader(String name) {
        [ 'Content-Security-Policy', 'Content-Length', 'Transfer-Encoding', 'Accept-Ranges', 'Date' ].any { it.equalsIgnoreCase(name) }
    }
    
}
