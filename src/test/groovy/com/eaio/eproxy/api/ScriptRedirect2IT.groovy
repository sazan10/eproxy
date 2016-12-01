package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletResponse

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
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
@WebIntegrationTest(value = 'script.redirect=http://rah.com', randomPort = true)
class ScriptRedirect2IT {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Autowired
    ScriptRedirect redirect
    
    @Test
    void 'should redirect with a 301'() {
        boolean statusCalled = false, redirected = false
        HttpServletResponse response = [ setStatus: { int status ->
            statusCalled = true
            assertThat(status, is(301I))
        }, setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, is('http://rah.com')); redirected = true } } ] as HttpServletResponse
        redirect.redirect(response)
        errorCollector.checkThat(statusCalled, is(true))
        errorCollector.checkThat(redirected, is(true))
    }
    
}
