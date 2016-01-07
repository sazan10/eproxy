package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.mock.web.DelegatingServletOutputStream
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.Eproxy
import com.eaio.net.httpclient.ReEncoding

/**
 * Simulates disabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=1', randomPort = true)
class ProxyHTTPIT {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Autowired
    Proxy proxy = new Proxy(reEncoding: new ReEncoding())
    
    @Test
    void 'content-security-policy headers should be removed'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/github.com/johannburkard/eproxy' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> assertThat('Content-Security-Policy headers should be removed for now',
                name?.toLowerCase() ?: '', not(is('content-security-policy'))) },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('johannburkard/eproxy &middot; GitHub'))
    }
    
}
