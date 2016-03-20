package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
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
@WebIntegrationTest(value = 'cookies.enabled=false', randomPort = true)
class NoScriptRedirectIT {

    @Autowired
    NoScriptRedirect redirect
    
    @Test
    void 'should support URLs'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah'))
        } ] as HttpServletResponse
        redirect.redirect('http://www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'whitespace should be removed'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah'))
        } ] as HttpServletResponse
        redirect.redirect('   http://www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah\t ', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support missing scheme'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah'))
        } ] as HttpServletResponse
        redirect.redirect('www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }

}
