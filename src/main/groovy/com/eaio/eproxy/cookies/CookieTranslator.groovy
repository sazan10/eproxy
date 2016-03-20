package com.eaio.eproxy.cookies

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

import org.apache.http.Header
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.cookie.CookieOrigin
import org.apache.http.cookie.CookieSpec
import org.apache.http.impl.cookie.DefaultCookieSpec
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Maps cookies from the client to cookies sent to the server.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@ConditionalOnProperty(name = 'cookies.enabled')
@Component
class CookieTranslator {
    
    @Lazy
    CookieSpec cookieSpec = new DefaultCookieSpec()

    void addToRequest(Cookie[] cookies, URI baseURI, URI requestURI, HttpUriRequest request) {
        cookies.collect {
            
        }
    }

    void addToResponse(Header[] headers, URI baseURI, URI requestURI, HttpServletResponse response) {
        CookieOrigin cookieOrigin = createCookieOrigin(requestURI)
        headers.findAll { it.name?.equalsIgnoreCase('Set-Cookie') }.each {
            
        }
    }
    
    CookieOrigin createCookieOrigin(URI requestURI) {
        new CookieOrigin(requestURI.host, requestURI.port < 0I && requestURI.scheme.equalsIgnoreCase('http') ? 80I : requestURI.port < 0I && requestURI.scheme.equalsIgnoreCase('https') ? 443I : requestURI.port,
            requestURI.rawPath, requestURI.scheme?.equalsIgnoreCase('https'))
    }

}
