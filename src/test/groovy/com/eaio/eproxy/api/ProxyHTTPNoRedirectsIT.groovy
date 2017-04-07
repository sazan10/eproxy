package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
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
@WebIntegrationTest(value = [ 'cookies.enabled=false' ], randomPort = true)
class ProxyHTTPNoRedirectsIT {

    @Autowired
    Proxy proxy
    
    @Test
    void 'redirect URLs should be rewritten'() {
        HttpServletRequest request = buildHttpServletRequest('http://n-tv.de/', 'GET', { String name -> null }, null, null)
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/http/www.n-tv.de/')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }
    
    @Test
    void 'rewriteConfig should be kept when redirecting'() {
        HttpServletRequest request = buildHttpServletRequest('http://n-tv.de/')
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/rnw-http/www.n-tv.de/')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }
    
    @Test
    void 'relative redirects should be supported 1'() {
        HttpServletRequest request = buildHttpServletRequest('http://edition.cnn.com/guides')
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/rnw-http/edition.cnn.com/specials/travel/guides')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }
    
    @Test
    void 'relative redirects should be supported 2'() {
        HttpServletRequest request = buildHttpServletRequest('http://www.3fm.nl/a/ug/263127')
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name?.toLowerCase() == 'location') { assertThat(value, is('http://fnuh.com/rnw-http/www.npo3fm.nl/radio/uitzendingen/263127')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }

}
