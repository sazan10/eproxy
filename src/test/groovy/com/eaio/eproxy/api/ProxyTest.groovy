package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.http.HeaderElement
import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.net.httpclient.ReEncoding

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class ProxyTest {
    
    @Lazy
    Proxy proxy = new Proxy(reEncoding: new ReEncoding())

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
            [ 'http', '/http/www.google-analytics.com', null, 'http://www.google-analytics.com/' ],
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
    void 'parseContentDispositionValue should find attachment'() {
        assertThat(proxy.parseContentDispositionValue(null), is(null))
        
        HeaderElement element = proxy.parseContentDispositionValue('ATTACHMENT;filename=bla.jpg')
        assertThat(element.name.toLowerCase(), is('attachment'))
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
    
}
