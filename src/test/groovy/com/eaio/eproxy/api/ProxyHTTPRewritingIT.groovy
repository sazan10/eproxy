package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

import com.eaio.eproxy.Eproxy

/**
 * Simulates enabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = [ 'proxy.javaScript.enabled=false', 'cookies.enabled=false' ], randomPort = true)
class ProxyHTTPRewritingIT {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Autowired
    Proxy proxy
    
    @Test
    void 'HTML should be rewritten'() {
        HttpServletRequest request = buildHttpServletRequest('http://www.n-tv.de')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString('<script')))
    }
    
    @Test
    void 'Google Font API URLs should be rewritten'() {
        HttpServletRequest request = buildHttpServletRequest('http://www.google.com/intl/en/policies/privacy/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('<link href="http://fnuh.com/rnw-http/fonts.googleapis.com/css?family=RobotoDraft:300,400,500,700,italic%7CProduct+Sans:400&lang=en"'))
    }
    
    @Test
    void 'Google Font API URLs should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('http://fonts.googleapis.com/css?family=RobotoDraft:300,400,500,700,italic%7CProduct+Sans:400&lang=en')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(bOut.toString(0I), containsString(' * See: https://www.google.com/fonts/license/productsans'))
    }
    
    @Test
    void 'forms should be rewritten'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.bing.com/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('action="http://fnuh.com/rnw-http/www.bing.com/search"'))
    }
    
    @Test
    void 'all on* handlers should be removed'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.google.de/url?q=http://fnuh.com')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), allOf(not(containsString(' on')), not(containsString(' ON'))))
    }
    
    @Test
    void '<noscript> contents should be removed'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.facebook.com/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), not(containsString('http-equiv="refresh"')))
    }
    
    @Test
    void 'SVG should be rewritten 1'() {
        HttpServletRequest request = buildHttpServletRequest('http://repo.eaio.com/Testing%20Cascade%20of%20SVG%20fill%20with%20external%20resource.html') // Originally from https://css-tricks.com/examples/svg-external-cascade/
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        errorCollector.checkThat(bOut.toString(0I), containsString('<code>&lt;use xlink:href=&quot;sprite.svg#dog&quot;'))
        errorCollector.checkThat(bOut.toString(0I), containsString('<use xlink:href="'))
    }
    
    @Test
    void 'CSS should be rewritten 2'() {
        HttpServletRequest request = buildHttpServletRequest('https://static.xx.fbcdn.net/rsrc.php/v2/yL/r/cExaeQ07vMA.css')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('src:url(data:font/opentype;base64,T1RUTwAJAIAAA'))
    }
    
    @Test
    void 'invalid BOMs should be ignored'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.deepdotweb.com/wp-content/themes/sahifa-child/style.css?ver=20150710')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('tiefontello'))
    }
    
    @Test
    void 'all <noscript> contents should be removed'() {
        HttpServletRequest request = buildHttpServletRequest('https://twitter.com/intent/user?screen_name=johannburkard')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), allOf(not(containsString('display:block')), not(containsString('display: block'))))
    }
    
    @Test
    void 'GitHub\'s sign in button should not be changed'() {
        HttpServletRequest request = buildHttpServletRequest('https://github.com/login?return_to=%2Fjohannburkard%2Ftinymeasurement')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('value="Sign in"'))
    }
    
    @Test
    void 'query strings should be preserved'() {
        HttpServletRequest request = buildHttpServletRequest('http://www.webpagefx.com/tools/read-able/check.php?uri=https%3A%2F%2Fgithub.com%2Fjohannburkard%2Feproxy')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString('Sorry! We can\'t get to that page')))
    }
    
    @Test
    void 'encoding for should be correct'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.bahn.de/p/view/index.shtml')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('Mobilit&auml;tsportal'))
    }
    
    @Test
    void 'Transfer-Encoding header should not be sent'() {
        HttpServletRequest request = buildHttpServletRequest('http://www.ip.de/lp/datenschutzinfo_online-werbung.cfm')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> assertThat(name?.toLowerCase(), not(is('transfer-encoding'))) },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('Datenschutzinfo'))
    }
    
    @Test
    void 'srcset attributes shold be rewritten'() {
        HttpServletRequest request = buildHttpServletRequest('https://en.wikipedia.org/wiki/Ketamine')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), not(containsString('//upload.wikimedia.org/wikipedia/commons/thumb/5/56/Ketamine.svg/300px-Ketamine.svg.png')))
    }
    
    @Test
    void 'SVG should be rewritten 2'() {
        HttpServletRequest request = buildHttpServletRequest('https://web.archive.org/web/20160401213553/https://en.wikipedia.org/static/1.27.0-wmf.13/skins/Vector/images/user-icon.svg?7b5d5')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('<path fill="#777777"'))
    }
    
    @Test
    void 'CSS should be rewritten 1'() {
        HttpServletRequest request = buildHttpServletRequest('https://bill.ccbill.com/jpost/jquery/css/smoothness/jquery-ui-1.7.2.custom.css')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('.ui-tabs .ui-tabs-hide'))
    }
    
    @Test
    void 'SVG elements should be closed if necessary'() {
        HttpServletRequest request = buildHttpServletRequest('http://tutorials.jenkov.com/svg/image-element.html')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        errorCollector.checkThat(bOut.toString(0I), anyOf(
            containsString('<rect x="10" y="10" height="130" width="500" style="fill: #000000"/>'),
            containsString('<rect x="10" y="10" height="130" width="500" style="fill: #000000"></rect>')))
        errorCollector.checkThat(bOut.toString(0I), anyOf(
            containsString('<image x="20" y="20" width="300" height="80" xlink:href="http://fnuh.com/rnw-http/jenkov.com/images/layout/top-bar-logo.png" />'),
            containsString('<image x="20" y="20" width="300" height="80" xlink:href="http://fnuh.com/rnw-http/jenkov.com/images/layout/top-bar-logo.png"></image>')))
        errorCollector.checkThat(bOut.toString(0I), anyOf(
            containsString('<line x1="25" y1="80" x2="350" y2="80" style="stroke: #ffffff; stroke-width: 3;"/>'),
            containsString('<line x1="25" y1="80" x2="350" y2="80" style="stroke: #ffffff; stroke-width: 3;"></line>')))
    }
    
    @Test
    void 'CSS rewriting should unescape HTML'() {
        HttpServletRequest request = buildHttpServletRequest('http://repo.eaio.com/leak.html')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('url(http://fnuh.com/rnw-https/leaking.via/inline-css-background-image)'))
    }
    
    @Test
    void 'all style attributes should be rewritten'() {
        HttpServletRequest request = buildHttpServletRequest('http://repo.eaio.com/%20Islam%20dan%20Kesimbangan%20Alam%20%E2%80%93%20Ustadz%20Riyadh%20bin%20Badr%20Bajrey.%20%E2%80%93%20Hidayah%20Sunnah%20Indonesia.html') // Saved from http://www.hidayahsunnah.com/blog/2014/10/01/ceramah-singkat-islam-dan-kesimbangan-alam-ustadz-riyadh-bin-badr-bajrey/
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), not(containsString('url(http://www.hidayahsunnah.com/wp-content/plugins/wp-socializer/public/social-icons/wp-socializer-sprite-32px.png?v1)')))
    }
    
    @Test
    void 'CSS rewriting should keep quotes'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.google.com/about/products/default.css')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), allOf(containsString('@import"http://fnuh.com/rnw-https/www.google.com/css/maia.css"'),
            containsString('@import"http://fnuh.com/rnw-https/www.google.com/about/css/default.css"'),
            containsString('@import"http://fnuh.com/rnw-https/fonts.googleapis.com/css?family=Open+Sans:300,600,700"')))
    }
    
    @Test
    void 'SVG rewriting should keep XML namespaces'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.google.com/search/about/img/badges/ios/badge.svg')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('xmlns:i="'))
        assertThat(bOut.toString(0I), containsString('''<svg version="1.1" id="US_UK_Download_on_the" x="0px" y="0px"
    viewBox="-57.5 -17 250 74.1"
    enable-background="new -57.5 -17 250 74.1" xml:space="preserve"
    xmlns:x="http://ns.adobe.com/Extensibility/1.0/"
    xmlns:i="http://ns.adobe.com/AdobeIllustrator/10.0/"
    xmlns:graph="http://ns.adobe.com/Graphs/1.0/"
    xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink">'''))
    }
    
}
