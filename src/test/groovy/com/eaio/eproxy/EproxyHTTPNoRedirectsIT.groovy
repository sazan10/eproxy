package com.eaio.eproxy

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.output.NullOutputStream
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.mock.web.DelegatingServletOutputStream
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.api.Proxy

/**
 * Simulates disabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=0', randomPort = true)
class EproxyHTTPNoRedirectsIT {

    @Autowired
    Eproxy eproxy
    
    @Autowired
    HttpClient httpClient
    
    @Autowired
    Proxy proxy
    
    @Test
    void 'redirects should be turned off'() {
        assertThat(eproxy.maxRedirects, is(0I))
    }
    
    @Test
    void 'request to http://n-tv.de should return a redirect'() {
        HttpCacheContext context = HttpCacheContext.create()
        HttpResponse response
        try {
            HttpGet get = new HttpGet('http://n-tv.de')
            response = httpClient.execute(get, context)
            assertThat(response.statusLine.statusCode, anyOf(is(301I), is(302I)))
        }
        finally {
            EntityUtils.consumeQuietly(response?.entity)
        }
    }
    
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
        ] as HttpServletRequest
        boolean statusSet = false, redirected = false
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://fnuh.com/http/www.n-tv.de/')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(NullOutputStream.NULL_OUTPUT_STREAM) },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }

}
