package com.eaio.eproxy.cookies

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.Cookie

import org.apache.http.cookie.Cookie as HCCookie
import org.apache.http.cookie.CookieOrigin
import org.apache.http.message.BasicHeader
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
@WebIntegrationTest(randomPort = true)
class CookieTranslatorIT {

    @Autowired
    CookieTranslator cookieTranslator

    @Test
    void 'should be able to parse PayPal\'s cookies'() {
        testCookieHandling('X-PP-SILOVER=name%3DLIVE5.WEB.1%26silo_version%3D880%26app%3Dappdispatcher%26TIME%3D3276278103%26HTTP_X_PP_AZ_LOCATOR%3D; Expires=Sat, 09 Apr 2016 18:54:03 GMT; domain=.paypal.com; path=/; Secure; HttpOnly')
    }
    
    @Test
    void 'should be able to parse flickr\'s cookies'() {
        testCookieHandling('xb=251229; Domain=.flickr.com; Path=/; Expires=Sun, 09 Apr 2017 12:31:47 GMT')
    }
    
    @Test
    void 'should be able to parse juicyads\'s cookies'() {
        testCookieHandling('visid_incap_165243=Puv0iipNQ4qrwGg4fW/CFdeiCFcAA')
    }
    
    void testCookieHandling(String cookieValue) {
        CookieOrigin origin = cookieTranslator.createCookieOrigin('https://www.paypal.com'.toURI())
        List<HCCookie> cookies = cookieTranslator.cookieSpec.parse(new BasicHeader('Set-Cookie', cookieValue), origin)
        assertThat(cookies, not(emptyIterable()))
    }
    
    @Test
    void 'should skip broken IDN URIs'() {
        cookieTranslator.addToRequest([ new Cookie('foo', 'bar') ] as Cookie[], 'http://bla.com'.toURI(),
            'http://xn--abc%20abc%20abc%20abc%20abc-.xn--abc%20-.net'.toURI(), null)
    }

}
