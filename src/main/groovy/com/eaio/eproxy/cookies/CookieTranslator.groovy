package com.eaio.eproxy.cookies

import static org.apache.commons.lang3.StringUtils.*
import groovy.util.logging.Slf4j

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.Header
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.cookie.ClientCookie
import org.apache.http.cookie.CookieOrigin
import org.apache.http.cookie.MalformedCookieException
import org.apache.http.impl.cookie.BasicClientCookie
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
@Slf4j
class CookieTranslator {

    @Lazy
    DefaultCookieSpec cookieSpec

    void addToRequest(Cookie[] cookies, URI baseURI, URI requestURI, HttpUriRequest request) {
        if (cookies) {
            CookieOrigin cookieOrigin = createCookieOrigin(requestURI)
            long now = System.currentTimeMillis()
            List<org.apache.http.cookie.Cookie> httpClientCookies = cookies.collect {
                decodeHttpClientCookie(toHttpClientCookie(it, now))
            }.findAll { org.apache.http.cookie.Cookie cookie ->
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
        List<org.apache.http.cookie.Cookie> cookies = new ArrayList<>()
        headers.findAll { it.name?.equalsIgnoreCase('Set-Cookie') }.each { Header header ->
            try {
                cookies.addAll(cookieSpec.parse(header, cookieOrigin))
            }
            catch (MalformedCookieException ex) {
                log.info('couldn\'t convert {}: {} to a cookie: {}', header.name, header.value, (ExceptionUtils.getRootCause(ex) ?: ex).message)
            }
        }
        if (cookies) {
            long now = System.currentTimeMillis()
            cookies.collect { toServletCookie(encodeHttpClientCookie(it), now) }.each {
                log.debug('adding response cookie {}={}', it.name, it.value)
                response.addCookie(it)
            }
        }
    }

    CookieOrigin createCookieOrigin(URI requestURI) {
        new CookieOrigin(requestURI.host, requestURI.port < 0I && requestURI.scheme.equalsIgnoreCase('http') ? 80I : requestURI.port < 0I && requestURI.scheme.equalsIgnoreCase('https') ? 443I : requestURI.port,
                requestURI.rawPath, requestURI.scheme?.equalsIgnoreCase('https'))
    }

    org.apache.http.cookie.Cookie decodeHttpClientCookie(org.apache.http.cookie.Cookie cookie) {
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

    org.apache.http.cookie.Cookie encodeHttpClientCookie(org.apache.http.cookie.Cookie cookie) {
        org.apache.http.cookie.Cookie out = new BasicClientCookie("${cookie.domain}_${cookie.name}", cookie.value)
        out.with {
            comment = cookie.comment
            path = cookie.path // TODO
            secure = cookie.secure // TODO: Allow for non-HTTPs proxies
            version = cookie.version
        }
        out
    }

    Cookie toServletCookie(org.apache.http.cookie.Cookie cookie, long now = System.currentTimeMillis()) {
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

    org.apache.http.cookie.Cookie toHttpClientCookie(Cookie cookie, long now = System.currentTimeMillis()) {
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
