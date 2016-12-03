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
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('http://www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'whitespace should be removed'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('   http://www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah\t ', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support missing scheme'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('www.n-tv.de/politik/Trump-und-Clinton-setzen-sich-durch-article17045231.html?ah#rah', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support Vitaly Popov\'s referrer spamming URL 1'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/Secret.xn--oogle-wmc.com'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('Secret.ɢoogle.com', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support Vitaly Popov\'s referrer spamming URL 2'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-https/Secret.xn--oogle-wmc.com'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('https://Secret.ɢoogle.com', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support Vitaly Popov\'s referrer spamming URL 3'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/Secret.xn--oogle-wmc.com/ah'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('Secret.ɢoogle.com/ah', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support Vitaly Popov\'s referrer spamming URL 4'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-https/Secret.xn--oogle-wmc.com/ah'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('https://Secret.ɢoogle.com/ah', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support Vitaly Popov\'s referrer spamming URL 5'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-http/Secret.xn--oogle-wmc.com/ah?fuh#guh'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('Secret.ɢoogle.com/ah?fuh#guh', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should support Vitaly Popov\'s referrer spamming URL 6'() {
        boolean sendRedirectCalled = false
        HttpServletResponse response = [ sendRedirect: { String location ->
            sendRedirectCalled = true
            assertThat(location, is('http://fnuh.com/rnw-https/Secret.xn--oogle-wmc.com/ah?fuh#guh'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('https://Secret.ɢoogle.com/ah?fuh#guh', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
    @Test
    void 'should redirect back to referrer if an empty URL is used'() {
        boolean sendRedirectCalled = false
                HttpServletResponse response = [ sendRedirect: { String location ->
                sendRedirectCalled = true
                assertThat(location, is('http://fnuh.com'))
        }, setHeader: { String name, String value -> } ] as HttpServletResponse
        redirect.redirect('', 'rnw', buildHttpServletRequest('uiuiui'), response)
        assertThat(sendRedirectCalled, is(true))
    }
    
}
