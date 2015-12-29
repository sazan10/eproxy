package com.eaio.eproxy.api

import groovy.util.logging.Slf4j

import java.nio.charset.Charset

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.Header
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.*
import org.apache.http.entity.ContentType
import org.apache.http.entity.InputStreamEntity
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import org.xml.sax.SAXException

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.*
import com.eaio.eproxy.rewriting.html.*
import com.eaio.net.httpclient.AbortHttpUriRequestTask
import com.eaio.net.httpclient.TimingInterceptor

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
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE)
            return
        }

        HttpCacheContext context = HttpCacheContext.create()
        HttpResponse remoteResponse
        try {
            HttpUriRequest uriRequest = newRequest(request.method, requestURI)
            addRequestHeaders(uriRequest, request)
            if (uriRequest instanceof HttpEntityEnclosingRequest) {
                setRequestEntity(uriRequest, request.getHeader('Content-Length'), request.inputStream)
            }
            
            if (totalTimeout) {
                timer?.schedule(new AbortHttpUriRequestTask(uriRequest), totalTimeout)
            }
            
            remoteResponse = httpClient.execute(uriRequest, context)
            
            response.setStatus(remoteResponse.statusLine.statusCode, remoteResponse.statusLine.reasonPhrase)
            
            remoteResponse.headerIterator().each { Header header ->
                if (header.name?.equalsIgnoreCase('Location')) { // TODO: Link and Refresh:, CORS headers ...
                    response.setHeader(header.name, rewrite(baseURI, resolve(requestURI, header.value), rewriteConfig ? new RewriteConfig(rewrite: true) : null) as String)
                }
                else { // TODO Header whitelist
                    response.setHeader(header.name, header.value)
                }
            }

            if (remoteResponse.entity) {
                ContentType contentType = ContentType.getLenient(remoteResponse.entity)
                Charset charset = contentType?.charset ?: Charset.forName('UTF-8')
                OutputStream outputStream = response.outputStream
                if (rewriting.canRewrite(rewriteConfig ? new RewriteConfig(rewrite: true) : null, contentType?.mimeType)) {
                    rewriting.rewrite(remoteResponse.entity.content, outputStream, charset, baseURI, requestURI, new RewriteConfig(rewrite: true), contentType.mimeType)
                }
                else {
                    IOUtils.copyLarge(remoteResponse.entity.content, outputStream) // Do not use HttpEntity#writeTo(OutputStream) -- doesn't get counted in all instances.
                }
            }            
        }
        catch (UnknownHostException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ExceptionUtils.getRootCauseMessage(ex))
        }
        catch (IllegalStateException ex) {
            // ignored
        }
        catch (SocketException ex) {
            if (ex.message?.startsWith('Permission denied')) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, ExceptionUtils.getRootCauseMessage(ex))
            }
            else {
                throw ex
            }
        }
        catch (SAXException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ExceptionUtils.getRootCauseMessage(ex))
        }
        finally {
            TimingInterceptor.log(context, log)
            EntityUtils.consumeQuietly(remoteResponse?.entity)
        }
    }
    
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
    URI buildRequestURI(String scheme, String requestURI, String queryString) {
        String host, path
        host = StringUtils.substringAfter(requestURI[1..-1], '/')
        path = StringUtils.substringAfter(host, '/') ?: '/'
        // TODO: Support for Ports
        UriComponentsBuilder.newInstance().scheme(scheme).host(StringUtils.substringBefore(host, '/')).path(path).query(queryString).build().toUri()
    }
    
    /**
     * Removes the context path prefix from <tt>requestURI</tt>.
     */
    String stripContextPathFromRequestURI(String contextPath, String requestURI) {
        contextPath ? StringUtils.substringAfter(requestURI, contextPath) : requestURI
    }
    
    int getPort(String scheme, int port) {
        port == -1I || (scheme == 'http' && port == 80I) || (scheme == 'https' && port == 443I) ? -1I : port
    }
    
    void setRequestEntity(HttpEntityEnclosingRequest uriRequest, String contentLength, InputStream inputStream) {
        uriRequest.entity = new InputStreamEntity(inputStream, contentLength?.isLong() ? contentLength as long : -1L)
    }
    
    void addRequestHeaders(HttpUriRequest uriRequest, HttpServletRequest request) {
        [ 'Accept', 'Accept-Language' ].each {
            if (request.getHeader(it)) {
                uriRequest.setHeader(it, request.getHeader(it))
            }
        }
    }
    

}
