package com.eaio.eproxy

import javax.servlet.http.HttpServletRequest

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Builds fake {@link HttpServletRequest requests}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RequestMocks {

    static HttpServletRequest buildHttpServletRequest(String uriString, String method = 'GET', Closure getHeaderClosure = { String name -> null },
            InputStream stream = null, RewriteConfig rewriteConfig = RewriteConfig.fromString('rnw')) {

        URI uri = URI.create(uriString)
        String encodedURI = (rewriteConfig ? '/rnw-' : '/' ) + uri.scheme + '/' + (uri.host + (uri.port == -1I ? '' : ':' + uri.port)) + uri.rawPath
        buildHttpServletRequestFromRawURI(encodedURI, uri.rawQuery, method, getHeaderClosure, stream, rewriteConfig)
    }


    static HttpServletRequest buildHttpServletRequestFromRawURI(String uriString, String queryString = null, String method = 'GET', Closure getHeaderClosure = { String name -> null },
            InputStream stream = null, RewriteConfig rewriteConfig = RewriteConfig.fromString('rnw')) {
        HttpServletRequest request = [
            getRequestURI: { uriString },
            getContextPath: { '' },
            getQueryString: { queryString },
            getMethod: { method },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: getHeaderClosure,
            getInputStream: { stream },
            getCookies: { null },
            setAttribute: { String name, Object value -> },
        ] as HttpServletRequest
    }

}
