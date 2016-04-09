package com.eaio.eproxy.cookies

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.Header
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.cookie.Cookie as HCCookie
import org.apache.http.cookie.*
import org.apache.http.impl.cookie.BasicClientCookie
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Maps cookies from the client to cookies sent to the server.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@ConditionalOnProperty(name = 'cookies.enabled')
@Component
@Slf4j
class CookieTranslator {

    @Autowired
    CookieSpec cookieSpec

    void addToRequest(Cookie[] cookies, URI baseURI, URI requestURI, HttpUriRequest request) {
        if (cookies) {
            CookieOrigin cookieOrigin = createCookieOrigin(requestURI)
            long now = System.currentTimeMillis()
            List<HCCookie> httpClientCookies = (List<HCCookie>) cookies.collect { Cookie cookie ->
                decodeHttpClientCookie(toHttpClientCookie(cookie, now))
            }.findAll { HCCookie cookie ->
                cookieSpec.match(cookie, cookieOrigin)
            }
            if (httpClientCookies) {
                cookieSpec.formatCookies(httpClientCookies).each {
                    log.debug('adding client cookie {}', it)
                    request.addHeader(it)
                }
            }
        }
    }

    void addToResponse(Header[] headers, URI baseURI, URI requestURI, HttpServletResponse response) {
        CookieOrigin cookieOrigin = createCookieOrigin(requestURI)
        List<HCCookie> cookies = new ArrayList<>()
        headers.findAll { it.name?.equalsIgnoreCase('Set-Cookie') }.each { Header header ->
            try {
                cookies.addAll(cookieSpec.parse(header, cookieOrigin))
            }
            catch (MalformedCookieException ex) {
                log.warn('couldn\'t convert {}: {} to a cookie: {}', header.name, header.value, (ExceptionUtils.getRootCause(ex) ?: ex).message)
            }
        }
        if (cookies) {
            long now = System.currentTimeMillis()
            cookies.collect { toServletCookie(encodeHttpClientCookie(it, baseURI), now) }.each {
                log.debug('adding response cookie {}={}', it.name, it.value)
                response.addCookie(it)
            }
        }
    }

    CookieOrigin createCookieOrigin(URI requestURI) {
        new CookieOrigin(requestURI.host, requestURI.port < 0I && requestURI.scheme.equalsIgnoreCase('http') ? 80I : requestURI.port < 0I && requestURI.scheme.equalsIgnoreCase('https') ? 443I : requestURI.port,
                requestURI.rawPath, requestURI.scheme?.equalsIgnoreCase('https'))
    }

    private HCCookie decodeHttpClientCookie(HCCookie cookie) {
        BasicClientCookie out = new BasicClientCookie(substringAfter(cookie.name, '_'), cookie.value)
        out.with {
            comment = cookie.comment
            domain = substringBefore(cookie.name, '_')
            path = cookie.path // TODO
            secure = cookie.secure // TODO: Allow for non-HTTPs proxies
            version = cookie.version
        }
        out.setAttribute(ClientCookie.DOMAIN_ATTR, out.domain)
        out
    }

    private HCCookie encodeHttpClientCookie(HCCookie cookie, URI baseURI) {
        HCCookie out = new BasicClientCookie("${cookie.domain}_${cookie.name}", cookie.value)
        out.with {
            comment = cookie.comment
            path = substringBeforeLast(baseURI.rawPath, '/') + cookie.path
            secure = cookie.secure // TODO: Allow for non-HTTPs proxies
            version = cookie.version
            expiryDate = cookie.expiryDate
        }
        out
    }

    private Cookie toServletCookie(HCCookie cookie, long now = System.currentTimeMillis()) {
        Cookie out = new Cookie(cookie.name, cookie.value)
        out.with {
            comment = cookie.comment
            path = cookie.path
            secure = cookie.secure
            version = cookie.version
        }
        if (cookie.domain) {
            out.domain = cookie.domain
        }
        if (cookie.expiryDate) {
            out.maxAge = (int) (cookie.expiryDate.time - now) / 1000I
        }
        out
    }

    private HCCookie toHttpClientCookie(Cookie cookie, long now = System.currentTimeMillis()) {
        BasicClientCookie out = new BasicClientCookie(cookie.name, cookie.value)
        out.with {
            comment = cookie.comment
            domain = cookie.domain
            path = cookie.path
            version = cookie.version
        }
        if (cookie.maxAge) {
            out.expiryDate = new Date(now + (cookie.maxAge * 1000I))
        }
        out
    }

}
