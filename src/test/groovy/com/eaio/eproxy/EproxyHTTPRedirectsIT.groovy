package com.eaio.eproxy

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

/**
 * Simulates enabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=1', randomPort=true)
class EproxyHTTPRedirectsIT {

    @Autowired
    Eproxy eproxy
    
    @Autowired
    HttpClient httpClient
    
    @Test
    void 'redirects should be turned on'() {
        assertThat(eproxy.maxRedirects, is(1I))
    }
    
    @Test
    void 'request to http://n-tv.de should return HTML'() {
        HttpCacheContext context = HttpCacheContext.create()
        HttpResponse response
        try {
            HttpGet get = new HttpGet('http://n-tv.de')
            response = httpClient.execute(get, context)
            assertThat(response.statusLine.statusCode, is(200I))
        }
        finally {
            EntityUtils.consumeQuietly(response?.entity)
        }
    }

}
