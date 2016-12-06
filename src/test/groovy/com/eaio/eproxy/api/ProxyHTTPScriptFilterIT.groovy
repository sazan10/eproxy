package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.mock.web.DelegatingServletOutputStream
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.Eproxy

/**
 * Simulates enabled scripts.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 * @see ScriptFilter
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = [ 'script.redirect=https://www.googletagmanager.com/gtm.js?id=GTM-M9CML7L', 'cookies.enabled=false' ], randomPort = true)
class ProxyHTTPScriptFilterIT {

    @Autowired
    Proxy proxy
    
    @Test
    void 'proxy JavaScript should be present on the page'() {
        HttpServletRequest request = buildHttpServletRequest('http://www.n-tv.de/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), containsString(' src="/script"></script>'))
    }

}
