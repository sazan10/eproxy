package com.eaio.eproxy.api

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLProtocolException
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
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpCoreContext
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

import com.eaio.eproxy.cookies.CookieTranslator
import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.*
import com.eaio.eproxy.rewriting.html.*
import com.eaio.io.RangeInputStream
import com.eaio.net.httpclient.AbortHttpUriRequestTask
import com.eaio.net.httpclient.TimingInterceptor
import com.google.apphosting.api.DeadlineExceededException
import com.google.apphosting.api.ApiProxy.CancelledException
import com.google.apphosting.api.ApiProxy.OverQuotaException
import com.j256.simplemagic.ContentInfo
import com.j256.simplemagic.ContentInfoUtil

/**
 * Proxies and optionally rewrites content.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@RestController
@Slf4j
class Proxy implements URIManipulation {

    // Note: Does not support multiple byte-range-sets.
    @Lazy
    private Pattern byte_ranges_specifier_1 = ~/(?i)^bytes=(\d+)-(\d*)$/, byte_ranges_specifier_2 = ~/(?i)^bytes=-(\d+)$/

    @Value('${http.totalTimeout}')
    Long totalTimeout

    @Value('${http.userAgent}')
    String userAgent
    
    @Value('${referrer.enabled}')
    boolean referrerEnabled

    @Autowired
    HttpClient httpClient

    @Autowired
    Rewriting rewriting

    @Autowired(required = false)
    Timer timer
    
    @Autowired(required = false)
    CookieTranslator cookieTranslator
    
    @Autowired
    ContentInfoUtil contentInfoUtil

    @RequestMapping('/{scheme:(?i)https?}/**')
    void proxy(@PathVariable String scheme, HttpServletRequest request, HttpServletResponse response) {
        proxy(null, scheme, request, response)
    }

    @RequestMapping('/{rewriteConfig:[a-z]+}-{scheme:(?i)https?}/**')
    void proxy(@PathVariable('rewriteConfig') String rewriteConfigString, @PathVariable('scheme') String scheme, HttpServletRequest request, HttpServletResponse response) {
        RewriteConfig rewriteConfig = RewriteConfig.fromString(rewriteConfigString)
        URI baseURI = buildBaseURI(request.scheme, request.serverName, request.serverPort, request.contextPath)
        request.setAttribute('baseURI', baseURI)
        URI requestURI = decodeTargetURI(scheme, stripContextPathFromRequestURI(request.contextPath, request.requestURI), request.queryString)
        request.setAttribute('requestURI', requestURI)
        
        if (!requestURI.rawPath) { // http://foo.com instead of http://foo.com/ (note trailing slash)
            String redirectURL = encodeTargetURI(baseURI, requestURI, requestURI.rawQuery ? '/?' + requestURI.rawQuery : '/', rewriteConfig)
            response.setHeader('Cache-Control', 'max-age=31536000')
            response.sendRedirect(redirectURL)
            return
        }

        HttpCacheContext context = HttpCacheContext.create()
        HttpResponse remoteResponse
        try {
            HttpUriRequest uriRequest = newRequest(request.method, requestURI)
            if (!uriRequest) {
                throw new MethodNotSupportedException(request.method)
            }
            
            addRequestHeaders(request, uriRequest)
            addReferrer(request, uriRequest, scheme, request.contextPath)
            cookieTranslator?.addToRequest(request.cookies, baseURI, requestURI, uriRequest)
            setRequestEntity(request, uriRequest)

            scheduleRequestTimeout(uriRequest)
            
            remoteResponse = httpClient.execute(uriRequest, context)
            requestURI = getTargetURI(context) ?: requestURI

            response.with {
                if (!committed) {
                    reset()
                }
                status = remoteResponse.statusLine.statusCode
            }

            String contentDisposition = parseContentDispositionValue(remoteResponse.getFirstHeader('Content-Disposition')?.value)
            ContentType contentType = ContentType.getLenient(remoteResponse.entity)
            
            if (shouldDetectMIMEType(contentType, remoteResponse.statusLine.statusCode, remoteResponse.entity?.repeatable)) {
                ContentInfo contentInfo = contentInfoUtil.findMatch(remoteResponse.entity.content)
                String detectedMIMEType = contentInfo?.mimeType ?: contentInfo?.contentType?.mimeType
                log.warn('no Content-Type header. Detected {}. Using MIME type {}', contentInfo, detectedMIMEType)
                contentType = ContentType.create(detectedMIMEType)
            }

            boolean canRewrite = rewriting.canRewrite(contentDisposition, rewriteConfig, contentType?.mimeType)
            
            copyRemoteResponseHeadersToResponse(remoteResponse.headerIterator(), response, canRewrite, baseURI, requestURI, rewriteConfig)
            cookieTranslator?.addToResponse(remoteResponse.allHeaders, baseURI, requestURI, response)

            if (remoteResponse.entity && remoteResponse.statusLine.statusCode < 300I) {
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

                OutputStream outputStream = response.outputStream
                if (canRewrite) {
                    if (range) {
                        // Don't allow range requests for rewritten content for now -- too unpredictable.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                    }
                    else {
                        rewriting.rewrite(remoteResponse.entity.content, outputStream, contentType?.charset, baseURI, requestURI, rewriteConfig, contentType?.mimeType)
                    }
                }
                else {
                    copyBinaryData(response, range, contentLength, remoteResponse.entity.content, outputStream)
                }
            }

            TimingInterceptor.log(context, log)
        }
        catch (SocketException ex) {
            if (ex.message?.startsWith('Permission denied')) { // Google App Engine
                throw new PermissionDeniedException(ex)
            }
        }
        catch (OutOfMemoryError err) {
            throw new OutOfMemoryException(err) // Thrown on Google App Engine when trying to allocate the buffer in IOUtils#copyLarge.
        }
        finally {
            try {
                EntityUtils.consumeQuietly(remoteResponse?.entity)
            }
            catch (emall) {}
        }
    }
    
    private boolean shouldDetectMIMEType(ContentType contentType, int statusCode, boolean entityIsRepeatable) {
        !contentType && statusCode < 300I && entityIsRepeatable
    }

    /**
     * Copies <tt>inputStream</tt> into <tt>outputStream</tt> with Range request support.
     */
    private copyBinaryData(HttpServletResponse response, List<Long> range, long contentLength, InputStream inputStream, OutputStream outputStream) {
        response.setHeader('Accept-Ranges', 'bytes')
        if (range) {
            response.status = HttpServletResponse.SC_PARTIAL_CONTENT
            response.setHeader('Content-Range', "bytes ${range[0]}-${range[1] - 1}/${contentLength}")
            response.setContentLength((int) range[1I] - range[0I])
        }

        // Do not use HttpEntity#writeTo(OutputStream) -- doesn't get counted in all instances.
        IOUtils.copyLarge(range ? new RangeInputStream(inputStream, range.get(0), range.get(1)) : inputStream, outputStream)
    }

    private scheduleRequestTimeout(HttpUriRequest uriRequest) {
        if (totalTimeout) {
            timer?.schedule(new AbortHttpUriRequestTask(uriRequest), totalTimeout)
        }
    }

    private copyRemoteResponseHeadersToResponse(HeaderIterator headers, HttpServletResponse response, boolean canRewrite, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        while (headers.hasNext()) {
            Header header = headers.nextHeader()
            if (header.name?.equalsIgnoreCase('Location')) { // TODO: Link and Refresh:, CORS headers ...
                response.setHeader(header.name, encodeTargetURI(baseURI, requestURI, header.value, rewriteConfig))
            }
            else if (shouldIncludeHeader(header.name, canRewrite)) {
                if (header.value?.contains('//')) {
                    log.warn('header may contain URL: {}: {}', header.name, header.value)
                }
                response.setHeader(header.name, header.value)
            }
        }
    }

    private HttpUriRequest newRequest(String method, URI uri) {
        switch (method) {
            case 'DELETE': return new HttpDelete(uri)
            case 'PATCH': return new HttpPatch(uri)
            case 'POST': return new HttpPost(uri)
            case 'PUT': return new HttpPut(uri)
            case 'GET': return new HttpGet(uri)
            case 'HEAD': return new HttpHead(uri)
            case 'OPTIONS': return new HttpOptions(uri)
            case 'TRACE': return new HttpTrace(uri)
        }
        log.warn('unsupported HTTP method {}', method)
    }

    /**
     * Sets the request body to <code>uriRequest</code>.
     */
    void setRequestEntity(HttpServletRequest request, HttpUriRequest uriRequest) {
        if (uriRequest instanceof HttpEntityEnclosingRequest) {
            String contentLength = request.getHeader('Content-Length') 
            uriRequest.entity = new InputStreamEntity(request.inputStream, contentLength?.isLong() ? contentLength as long : -1L)
        }
    }

    /**
     * Adds the following headers to the outgoing request:
     * <ul>
     * <li>Accept
     * <li>Accept-Language
     * <li>If-Modified-Since
     * <li>If-None-Match
     * </ul>
     * Also adds the User-Agent header if {@link #userAgent} isn't set.
     */
    void addRequestHeaders(HttpServletRequest request, HttpUriRequest uriRequest) {
        [ 'Accept', 'Accept-Language', 'If-Modified-Since', 'If-None-Match' ].each {
            if (request.getHeader(it)) {
                uriRequest.setHeader(it, request.getHeader(it))
            }
        }
        if (!userAgent && request.getHeader('User-Agent')) {
            uriRequest.setHeader('User-Agent', request.getHeader('User-Agent'))
        }
    }
    
    /**
     * If the <code>Referer</code> header contains a URL in eproxy's scheme, decode the original URL and send it as the new <code>Referer</code> header.
     */
    void addReferrer(HttpServletRequest request, HttpUriRequest uriRequest, String scheme, String contextPath) {
        if (referrerEnabled && request.getHeader('Referer')) {
            try {
                URI referrer = URI.create(request.getHeader('Referer'))
                URI decodedReferrer = decodeTargetURI(scheme, stripContextPathFromRequestURI(contextPath, referrer.path), referrer.query)
                if (decodedReferrer.host) {
                    uriRequest.setHeader('Referer', decodedReferrer as String)
                }
            }
            catch (emall) {}
        }
    }

    /**
     * Returns the lower-cased name of the Content-Disposition header value.
     * @return <code>null</code> if <code>contentDisposition</code> is <code>null</code>
     */
    String parseContentDispositionValue(String contentDisposition) {
        if (contentDisposition) {
            HeaderElement[] elements = BasicHeaderValueParser.parseElements(contentDisposition, null)
            elements[0I]?.name?.toLowerCase()
        }
    }

    /**
     * Returns whether a certain server-side header (ignoring case) should be included. Also drops <tt>Content-Length</tt> if rewriting.
     * <p>
     * TODO: This should probably be a whitelist instead of a blacklist.
     */
    boolean shouldIncludeHeader(String name, boolean canRewrite) {
        // Whitelist: Last-Modified, Content-Type
        switch (name?.toLowerCase()) {
            // TODO: Pass through if not filtering active content
            case 'access-control-allow-origin':
            case 'content-security-policy':
            case 'x-xss-protection': 
            case 'timing-allow-origin':
            // end
            case 'link':
            case 'via':
            case 'vary':
            case 'connection':
            case 'content-security-policy':
            case 'transfer-encoding':
            case 'date':
            case 'pragma':
            case 'set-cookie':
            case 'age':
            case 'p3p': return false
            case 'content-length': return !canRewrite
        }
        true
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

    /**
     * Extracts the URI of the last request (after following redirects) from the given {@link HttpContext}.
     *
     * @see com.eaio.net.httpclient.TimingInterceptor
     */
    URI getTargetURI(HttpContext context) {
        HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST)
        HttpHost currentHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST)
        currentReq.URI.absolute ? currentReq.URI : (currentHost.toURI() + currentReq.URI).toURI()
    }
    
    // Exception handling
    
    /**
     * This roughly means that the client has closed the connection.
     */
    @ExceptionHandler(IllegalStateException)
    void ignoredException(Throwable thrw, HttpServletRequest request, HttpServletResponse response) {
        log.warn('{}: {}', request.getAttribute('requestURI'), thrw)
    }
    
    /**
     * Catches "out of quota"-style exceptions on Google App Engine. Doesn't forward to the error JSP because if the request is over a quota already, that won't work.
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler([ DeadlineExceededException, CancelledException, OverQuotaException, OutOfMemoryException ])
    void serviceUnavailable(Throwable thrw, HttpServletRequest request, HttpServletResponse response) {
        log.warn('{}: {}', request.getAttribute('requestURI'), thrw)
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler([ NumberFormatException, URISyntaxException, ClientProtocolException ])
    ModelAndView badRequest(Throwable thrw, HttpServletRequest request, HttpServletResponse response) {
        log.warn('{}: {}', request.getAttribute('requestURI'), thrw)
        copyRequestAttributesToModelAndView(request, newErrorModelAndView(thrw, 400I))
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler([ HttpHostConnectException, SSLException, SSLHandshakeException, SSLProtocolException, NoHttpResponseException, SocketTimeoutException, ConnectTimeoutException, UnknownHostException ])
    ModelAndView notFound(Throwable thrw, HttpServletRequest request, HttpServletResponse response) {
        log.warn('{}: {}', request.getAttribute('requestURI'), thrw)
//  if ((ExceptionUtils.getRootCause(ex) ?: ex).message == 'Prime size must be multiple of 64, and can only range from 512 to 1024 (inclusive)') {
//      sendError(requestURI, response, HttpServletResponse.SC_FORBIDDEN, ex, "Please upgrade to Java 8. ${requestURI.host} uses more than 1024 Bits in their public key.")
//  }
        copyRequestAttributesToModelAndView(request, newErrorModelAndView(thrw, 404I))
    }
    
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler([ MethodNotSupportedException ])
    ModelAndView methodNotSupported(Throwable thrw, HttpServletRequest request, HttpServletResponse response) {
        log.warn('{}: {}', request.getAttribute('requestURI'), thrw)
        copyRequestAttributesToModelAndView(request, newErrorModelAndView(thrw, 405I))
    }
    
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler([ PermissionDeniedException ])
    ModelAndView forbidden(Throwable thrw, HttpServletRequest request, HttpServletResponse response) {
        log.warn('{}: {}', request.getAttribute('requestURI'), thrw)
        copyRequestAttributesToModelAndView(request, newErrorModelAndView(thrw, 403I))
    }
    
    private ModelAndView newErrorModelAndView(Throwable thrw, int status) {
        new ModelAndView('ouch', [ status: status, message: (ExceptionUtils.getRootCause(thrw) ?: thrw).message ])
    }
    
    private ModelAndView copyRequestAttributesToModelAndView(HttpServletRequest request, ModelAndView out) {
        Enumeration<String> e = request.attributeNames
        while (e.hasMoreElements()) {
            String s = e.nextElement()
            out.addObject(s, request.getAttribute(s))
        }
        out
    }
    
    @InheritConstructors
    private class PermissionDeniedException extends RuntimeException {}
    
    @InheritConstructors
    private class OutOfMemoryException extends RuntimeException {}
    
}
