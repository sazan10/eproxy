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

import com.eaio.eproxy.Eproxy

/**
 * Simulates disabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=0', randomPort = true)
class ProxyHTTPNoRedirectsIT {

    @Autowired
    Proxy proxy
    
    @Test
    void 'redirect URLs should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/http/n-tv.de' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/http/www.n-tv.de/')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }
    
    @Test
    void 'rewriteConfig should be kept when redirecting'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/n-tv.de' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/ah-http/www.n-tv.de/')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }
    
    @Test
    void 'relative redirects should be supported'() {
        HttpServletRequest request = [
            getRequestURI: { '/http/edition.cnn.com/guides' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/ah-http/edition.cnn.com/specials/travel/guides')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }

}
