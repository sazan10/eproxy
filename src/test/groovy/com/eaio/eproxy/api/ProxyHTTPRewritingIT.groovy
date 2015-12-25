package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.output.NullOutputStream
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.mock.web.DelegatingServletOutputStream
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.api.Proxy
import com.eaio.eproxy.Eproxy

/**
 * Simulates disabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=1', randomPort = true)
class ProxyHTTPRewritingIT {

    @Autowired
    Proxy proxy
    
    @Test
    void 'HTML should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/www.n-tv.de' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeaderNames: { Collections.EMPTY_LIST as Enumeration }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString('<script')))
    }
    
    @Test
    void 'Google Font API URLs should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/www.google.com/intl/en/policies/privacy/' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeaderNames: { Collections.EMPTY_LIST as Enumeration }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), allOf(containsString('<html'),
            containsString('<link href="http://fnuh.com/ah-http/fonts.googleapis.com/css?family=RobotoDraft:300,400,500,700,italic|Product+Sans:400&amp;lang=en"')))
    }
    
    @Test
    void 'Google Font API URLs should be supported'() {
        HttpServletRequest request = [
            getRequestURI: { '/http/fonts.googleapis.com/css' },
            getContextPath: { '' },
            getQueryString: { 'family=RobotoDraft:300,400,500,700,italic|Product+Sans:400&lang=en' },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeaderNames: { Collections.EMPTY_LIST as Enumeration }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(bOut.toString(0I), containsString(' * See: https://www.google.com/fonts/license/productsans'))
    }
    
    @Test
    void 'forms should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/www.google.com' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeaderNames: { Collections.EMPTY_LIST as Enumeration }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('action="http://fnuh.com/ah-http/www.google.com/search"'))
    }
    
    @Test
    void 'all on* handlers should be removed'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/www.google.de/url' },
            getContextPath: { '' },
            getQueryString: { 'q=http://fnuh.com' },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeaderNames: { Collections.EMPTY_LIST as Enumeration }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString('onmouse')))
    }
    
    /*// Currently broken due to TagSoup. Investigating...
    @Test
    void '<noscript> contents should be removed'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/www.facebook.com/' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeaderNames: { Collections.EMPTY_LIST as Enumeration }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'https', request, response)
        assertThat(bOut.toString(0I), not(containsString('<meta http-equiv="refresh" content="0; URL=/?_fb_noscript=1')))
    }
    */
    
}
