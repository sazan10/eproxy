package com.eaio.eproxy

import javax.servlet.http.HttpServletRequest

import com.eaio.eproxy.entities.RewriteConfig

class RequestMocks {

    static HttpServletRequest buildHttpServletRequest(String uriString, String method = 'GET', Closure getHeaderClosure = { String name -> null },
        InputStream stream = null, RewriteConfig rewriteConfig = RewriteConfig.fromString('rnw')) {
        
        URI uri = URI.create(uriString)
        
        HttpServletRequest request = [
            getRequestURI: { (rewriteConfig ? '/rnw-' : '/' ) + uri.scheme + '/' + (uri.host + (uri.port == -1I ? '' : ':' + uri.port)) + uri.rawPath },
            getContextPath: { '' },
            getQueryString: { uri.rawQuery },
            getMethod: { method },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: getHeaderClosure,
            getInputStream: { stream }
        ] as HttpServletRequest
    }

}
