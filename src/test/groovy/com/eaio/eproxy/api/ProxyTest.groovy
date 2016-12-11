package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest

import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.http.MethodNotSupportedException
import org.apache.http.client.methods.HttpUriRequest
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.eproxy.entities.*

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class ProxyTest {
    
    @Lazy
    Proxy proxy = new Proxy(referrerEnabled: true)

    /**
     * Test method for {@link com.eaio.web.Proxy#buildRequestURI(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    @Parameters(method = 'buildRequestURIParameters')
    void 'buildRequestURI should match expectation'(String scheme, String uri, String queryString, String expectation) {
        assertThat(proxy.decodeTargetURI(scheme, uri, queryString), is(expectation.toURI()))
    }
    
    Collection<Object[]> buildRequestURIParameters() {
        [
            [ 'http', '/http/www.google-analytics.com/ga.js', null, 'http://www.google-analytics.com/ga.js' ],
            [ 'http', '/http/www.google-analytics.com/ga.js', '', 'http://www.google-analytics.com/ga.js' ],
            [ 'http', '/http/www.google-analytics.com/ga.js', 'pruh=guh', 'http://www.google-analytics.com/ga.js?pruh=guh' ],
            [ 'http', '/http/www.google-analytics.com', null, 'http://www.google-analytics.com' ],
            [ 'https', '/https/www.google-analytics.com/bla/ga.js', null, 'https://www.google-analytics.com/bla/ga.js' ],
        ].collect { it as Object[] }
    }
    
    @Test
    @Parameters(method = 'stripContextPathFromRequestURIParameters')
    void 'stripContextPathFromRequestURIParameters should match expectation'(String contextPath, String requestURI, String expectation) {
        assertThat(proxy.stripContextPathFromRequestURI(contextPath, requestURI), is(expectation))
    }
    
    Collection<Object[]> stripContextPathFromRequestURIParameters() {
        [
            [ '', '/fnuh', '/fnuh' ],
            [ '/pruh', '/pruh/fnuh', '/fnuh' ],
            [ '/fnuh', '/fnuh/fnuh', '/fnuh' ],
        ].collect { it as Object[] }
    }
    
    @Test
    @Parameters(method = 'getPortParameters')
    void 'getPort should skip port 80 and 443'(String scheme, int port, int expectation) {
        assertThat(proxy.getPort(scheme, port), is(expectation))
    }
    
    Collection<Object[]> getPortParameters() {
        [
            [ 'https', 443I, -1I ],
            [ 'http', 80I, -1I ],
            [ 'http', 8080I, 8080I ],
            [ 'https', 1234I, 1234I ],
            [ 'http', 42I, 42I ],
            [ 'https', 8443I, 8443I ]
        ].collect { it as Object[] }
    }
    
    @Test
    @Parameters(method = 'contentDispositionValues')
    void 'parseContentDispositionValue should return expected value'(String contentDispositionValue, Matcher<String> expectedValue) {
        assertThat(proxy.parseContentDispositionValue(contentDispositionValue), expectedValue)
    }
    
    Collection<Object[]> contentDispositionValues() {
        [
            [ null, nullValue() ],
            [ 'ATTACHMENT;filename=bla.jpg', is('attachment') ],
            [ 'inline; filename="1%20%2857%29.jpg', is('inline') ],
            [ 'inline;filename=""', is('inline') ],
            [ 'attachment; filename="y7bw1528(http://maalud.wapka.mobi).mp4"', is('attachment') ],
            [ 'inline; filename="1%20%2857%29.jpg"', is('inline') ],
        ].collect { it as Object[] }
    }
    
    @Test
    void 'resolve should add scheme back'() {
        assertThat(proxy.resolve('http://foo.com/ah/oh.html'.toURI(), '/ui.html'), is('http://foo.com/ui.html'.toURI()))
    }
    
    @Test
    void 'broken URL encoding should be supported'() {
        assertThat(proxy.resolve('http://www.kajianilmiah.com'.toURI(), 'http://www.kajianilmiah.com/search/label/Tabshirul%20khalaf%20%28Masih%20Ahlussunnahkah%20Kita%20%3F%2'),
            is('http://www.kajianilmiah.com/search/label/Tabshirul%20khalaf%20%28Masih%20Ahlussunnahkah%20Kita%20%3F'.toURI()))
    }
    
    @Test
    void 'Vitaly Popov\'s referrer spamming URL should be supported'() {
        assertThat(proxy.encodeTargetURI(URI.create('http://127.0.0.1:8080'), URI.create('http://secret.xn--oogle-wmc.com/'), "http://money.get.away.get.a.good.job.with.more.pay.and.you.are.okay.money.it.is.a.gas.grab.that.cash.with.both.hands.and.make.a.stash.new.car.caviar.four.star.daydream.think.i.ll.buy.me.a.football.team.money.get.back.i.am.alright.jack.ilovevitaly.com/#.keep.off.my.stack.money.it.is.a.hit.do.not.give.me.that.do.goody.good.bullshit.i.am.in.the.hi.fidelity.first.class.travelling.set.and.i.think.i.need.a.lear.jet.money.it.is.a.secret.ɢoogle.com/#.share.it.fairly.but.dont.take.a.slice.of.my.pie.money.so.they.say.is.the.root.of.all.evil.today.but.if.you.ask.for.a.rise.it's.no.surprise.that.they.are.giving.none.and.secret.ɢoogle.com",
            RewriteConfig.fromString('rnw')), is('http://127.0.0.1:8080/rnw-http/money.get.away.get.a.good.job.with.more.pay.and.you.are.okay.money.it.is.a.gas.grab.that.cash.with.both.hands.and.make.a.stash.new.car.caviar.four.star.daydream.think.i.ll.buy.me.a.football.team.money.get.back.i.am.alright.jack.ilovevitaly.com/#.keep.off.my.stack.money.it.is.a.hit.do.not.give.me.that.do.goody.good.bullshit.i.am.in.the.hi.fidelity.first.class.travelling.set.and.i.think.i.need.a.lear.jet.money.it.is.a.secret.%C9%A2oogle.com/'))
    }
    
    @Test(expected = URISyntaxException)
    void 'should reject incomplete URLs'() {
        HttpServletRequest request = [
            getRequestURI: { '/rnw-http' },
            getContextPath: { '' },
            getQueryString: { null },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            setAttribute: { String name, Object value -> },
        ] as HttpServletRequest
    
        proxy.proxy('http', request, null)
    }
    
    /**
     * Make sure to send the scheme from the eproxy URL, not the request scheme in the referrer header.
     */
    @Test
    void 'should use the scheme from proxy URLs, not the request scheme'() {
        assertThat(proxy.referrerEnabled, is(true))
        HttpServletRequest request = [ getHeader: { 'https://foo.com/eproxy/rnw-http/rah.com/ui.html' } ] as HttpServletRequest
        
        boolean setHeaderCalled = false
        HttpUriRequest uriRequest = [ setHeader: { String name, String value -> setHeaderCalled = true; assertThat(name, is('Referer')); assertThat(value, is('http://rah.com/ui.html')) } ] as HttpUriRequest
        
        proxy.addReferrer(request, uriRequest, 'http', '/eproxy')
        
        assertThat(setHeaderCalled, is(true))
    }
    
    @Test(expected = MethodNotSupportedException)
    void 'weird HTTP methods shouldn\'t be supported'() {
        HttpServletRequest request = [
            getRequestURI: { '/rnw-http/blorb.com/' },
            getContextPath: { '' },
            getQueryString: { null },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            setAttribute: { String name, Object value -> },
            getMethod: { 'CONNECT' },
            getHeader: { String name -> null },
        ] as HttpServletRequest

        proxy.proxy('rnw', 'http', request, null)
    }
    
}
