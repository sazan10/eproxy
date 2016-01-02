package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.output.NullOutputStream
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.mock.web.DelegatingServletOutputStream
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.api.Proxy
import com.eaio.eproxy.Eproxy

/**
 * Simulates disabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=1', randomPort = true)
class ProxyHTTPRewritingIT {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Autowired
    Proxy proxy
    
    @Test
    void 'HTML should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/www.n-tv.de' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString('<script')))
    }
    
    @Test
    void 'Google Font API URLs should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/www.google.com/intl/en/policies/privacy/' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('<link href="http://fnuh.com/ah-http/fonts.googleapis.com/css?family=RobotoDraft:300,400,500,700,italic|Product+Sans:400&amp;lang=en"'))
    }
    
    @Test
    void 'Google Font API URLs should be supported'() {
        HttpServletRequest request = [
            getRequestURI: { '/http/fonts.googleapis.com/css' },
            getContextPath: { '' },
            getQueryString: { 'family=RobotoDraft:300,400,500,700,italic|Product+Sans:400&lang=en' },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(bOut.toString(0I), containsString(' * See: https://www.google.com/fonts/license/productsans'))
    }
    
    @Test
    void 'forms should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-http/www.google.com' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('action="http://fnuh.com/ah-http/www.google.com/search"'))
    }
    
    @Test
    void 'all on* handlers should be removed'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/www.google.de/url' },
            getContextPath: { '' },
            getQueryString: { 'q=http://fnuh.com' },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString(' on')))
    }
    
    @Test
    void '<noscript> contents should be removed'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/www.facebook.com/' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'https', request, response)
        assertThat(bOut.toString(0I), not(containsString('http-equiv="refresh"')))
    }
    
    @Test
    void 'SVG elements should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/css-tricks.com/examples/svg-external-cascade/' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'https', request, response)
        errorCollector.checkThat(bOut.toString(0I), anyOf(containsString('<code>&lt;use xlink:href="sprite.svg#dog"'),
            containsString('<code>&lt;use xlink:href=&quot;sprite.svg#dog&quot;')))
        errorCollector.checkThat(bOut.toString(0I), containsString('<use xlink:href="'))
    }
    
    @Test
    void 'CSS should be rewritten'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/static.xx.fbcdn.net/rsrc.php/v2/yL/r/EZnQqgEpw9Z.css' },
            getContextPath: { '' },
            getQueryString: { null },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'https', request, response)
        assertThat(bOut.toString(0I), anyOf(
            containsString('._4f7n{background-image:url(data:image/png;base64,iVBORw0KGgoAAAA'),
            containsString('._4f7n { background-image: url(data:image/png;base64,iVBORw0KGgoAAAA')))
    }
    
    @Test
    void 'invalid BOMs should be ignored'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/www.deepdotweb.com/wp-content/themes/sahifa-child/style.css' },
            getContextPath: { '' },
            getQueryString: { 'ver=20150710' },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('url(http://fnuh.com/ah-https/www.deepdotweb.com/wp-content/themes/sahifa-child/fonts/tiefontello.eot?88026028)'))
    }
    
    @Test
    void 'all <noscript> contents should be removed'() {
        HttpServletRequest request = [
            getRequestURI: { '/ah-https/twitter.com/intent/user' },
            getContextPath: { '' },
            getQueryString: { 'screen_name=johannburkard' },
            getMethod: { 'GET' },
            getScheme: { 'http' },
            getServerName: { 'fnuh.com' },
            getServerPort: { 80I },
            getHeader: { String name -> null }
        ] as HttpServletRequest
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String message -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
        ] as HttpServletResponse
        proxy.proxy('ah', 'https', request, response)
        assertThat(bOut.toString(0I), allOf(not(containsString('display:block')), not(containsString('display: block'))))
    }
    
}
