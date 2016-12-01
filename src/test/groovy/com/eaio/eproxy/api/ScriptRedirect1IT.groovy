package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletResponse

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.Eproxy

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'script.redirect=', randomPort = true)
class ScriptRedirect1IT {

    @Autowired
    ScriptRedirect redirect
    
    @Test
    void 'should return a 204'() {
        boolean statusCalled = false
        HttpServletResponse response = [ setStatus: { int status ->
            statusCalled = true
            assertThat(status, is(204I))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect(response)
        assertThat(statusCalled, is(true))
    }
    
}
