package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.eproxy.api.Proxy

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class ProxyTest {
    
    @Lazy
    Proxy proxy

    /**
     * Test method for {@link com.eaio.web.Proxy#buildRequestURI(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    @Parameters(method = 'buildRequestURIParameters')
    void 'buildRequestURI should match expectation'(String scheme, String uri, String queryString, String expectation) {
        assertThat(proxy.buildRequestURI(scheme, uri, queryString), is(expectation.toURI()))
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
    @Parameters(method = 'rewriteLocationValueParameters')
    void 'rewriteLocationValue should redirect URIs correctly'(String locationValue, String requestScheme, String requestHost, int requestPort, String contextPath, String expectation) {
        assertThat(proxy.rewriteLocationValue(locationValue.toURI(), requestScheme, requestHost, requestPort, contextPath), is (expectation.toURI()))
    }
    
    Collection<Object[]> rewriteLocationValueParameters() {
        [
            [ 'http://www.n-tv.de', 'https', 'fnuh.com', -1, '/ah', 'https://fnuh.com/ah/http/www.n-tv.de' ],
            [ 'http://www.n-tv.de/', 'https', 'fnuh.com', -1, '', 'https://fnuh.com/http/www.n-tv.de/' ],
            [ 'http://www.n-tv.de:81/', 'https', 'fnuh.com', -1, '', 'https://fnuh.com/http/www.n-tv.de:81/' ],
            [ 'http://www.n-tv.de/#rah', 'https', 'fnuh.com', -1, '', 'https://fnuh.com/http/www.n-tv.de/#rah' ],
            [ 'http://www.n-tv.de/?ah=ha#rah', 'https', 'fnuh.com', -1, '', 'https://fnuh.com/http/www.n-tv.de/?ah=ha#rah' ],
        ].collect { it as Object[] }
    }

}
