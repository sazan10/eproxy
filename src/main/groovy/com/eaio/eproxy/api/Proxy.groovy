package com.eaio.eproxy.api

import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.*
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.http.TimingLogger
import com.eaio.net.httpclient.TimingInterceptor

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RestController
@Slf4j
class Proxy {
    
    @Autowired
    HttpClient httpClient
    
    @Autowired
    TimingLogger timingLogger

    @RequestMapping(value = '/{scheme}/**')
    void proxy(@PathVariable String scheme, HttpServletRequest request, HttpServletResponse response) {
        URI requestURI = buildRequestURI(scheme, request.contextPath ? StringUtils.substringAfter(request.requestURI, request.contextPath) : request.requestURI, request.queryString)

        HttpCacheContext context = HttpCacheContext.create()
        HttpResponse httpResponse
        try {
            HttpUriRequest uriRequest = newRequest(request.method, requestURI)
            
            httpResponse = httpClient.execute(uriRequest, context)
            
            response.setStatus(httpResponse.statusLine.statusCode, httpResponse.statusLine.reasonPhrase)
            
            httpResponse.headerIterator().each { Header header ->
                response.setHeader(header.name, header.value)
            }

            if (httpResponse.entity) {
                IOUtils.copyLarge(httpResponse.entity.content, response.outputStream) // Do not use HttpEntity#writeTo(OutputStream) -- doesn't get counted in all instances.
            }            
            
        }
        catch (SocketException ex) {
            if (ex.message?.startsWith('Permission denied')) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.message)
            }
//            else if (ex.message?.startsWith('Connection reset')) {
//                Can be handled by enabling retries.
//            }
            else {
                throw ex
            }
        }
        finally {
            timingLogger.log(context, log)
            EntityUtils.consumeQuietly(httpResponse?.entity)
        }
    }
    
    private HttpUriRequest newRequest(String method, URI uri) {
        switch (method?.toUpperCase()) {
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
    
    /**
     * Make sure to remove the context path before calling this method.
     */
    // TODO: Use ReEncodingRedirectStrategy
    URI buildRequestURI(String scheme, String requestURI, String queryString) {
        String host, path
        host = StringUtils.substringAfter(requestURI[1..-1], '/')
        path = StringUtils.substringAfter(host, '/') ?: '/'
        
        UriComponentsBuilder.newInstance().scheme(scheme).host(StringUtils.substringBefore(host, '/')).path(path).query(queryString).build().toUri() // TODO: Support for Ports
    }

}
