package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
@WebIntegrationTest(value = [ 'cookies.enabled=false' ], randomPort = true)
class ProxyHTTPIT {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Autowired
    Proxy proxy

    @Test
    void 'content-security-policy headers should be removed'() {
        HttpServletRequest request = buildHttpServletRequest('https://github.com/johannburkard/eproxy')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> assertThat('Content-Security-Policy headers should be removed for now',
                name?.toLowerCase() ?: '', not(is('content-security-policy'))) },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('johannburkard/eproxy'))
    }
    
    @Test
    void 'range requests should return only a part of the response'() {
        HttpServletRequest request = buildHttpServletRequest('https://bilder1.n-tv.de/img/incoming/crop16474236/4269152083-cImg_17_6-w680/imago66342948h.jpg', 'GET', { String name -> name == 'Range' ? 'bytes=0-99' : null })
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(200I), is(206I))) }, // setStatus(int) is called twice
            setHeader: { String name, String value -> if (name == 'Content-Range') { assertThat(value, startsWith('bytes 0-99/')) } },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
            setContentLength: { int length -> assertThat(length, is(100I)) },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toByteArray().encodeBase64() as String, startsWith('/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAA'))
    }
    
    @Test
    void 'ports should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.bing.com:443/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('action="//fnuh.com/rnw-https/www.bing.com:443/search"'))
    }
    
    @Test
    void 'broken HTTPS should be supported 1'() {
        HttpServletRequest request = buildHttpServletRequest('https://about.me/johannburkard')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('Johann Burkard'))
    }
    
    @Test
    void 'broken HTTPS should be supported 2'() {
        HttpServletRequest request = buildHttpServletRequest('https://archive.org/details/MainHoonSarkarEMadinaKaGada')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('Free Download'))
    }
    
    @Test
    void 'broken redirects should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('http://bit.ly/19xbm5w')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(301I)) },
            setHeader: { String name, String value -> if (name == 'Location') assertThat(value, is('//fnuh.com/http/cprouvost.free.fr/fun/generiques/--%20Film%20--/Film%20-%20Star%20Wars%20(The%20Imperial%20March).mp3')) },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    @Test(expected = UnknownHostException)
    void 'non-existing domains should return a 404'() {
        HttpServletRequest request = buildHttpServletRequest('http://das-ist-absolut-cla.com/')
        proxy.proxy('http', request, null)
    }
    
    @Test
    void 'invalid range requests should return a 416'() {
        HttpServletRequest request = buildHttpServletRequest('https://eaio.com/robots.txt', 'GET', { String name -> name == 'Range' ? 'bytes=5000-' : null })
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        int calls = 0I
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) }, // status is first set to 200...
            sendError: { int status -> assertThat(status, is(416I)) }, // ...then to 416
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('https', request, response)
    }

    @Test
    void 'trace requests should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('https://repo.eaio.com/', 'TRACE')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(405I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('https', request, response)
    }
    
    @Test
    void 'uppercase protocol should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('HTTPS://www.n-tv.de/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('HTTPS', request, response)
        assertThat(bOut.toString(0I), containsString('Nachrichten'))
    }
    
    @Test
    void 'domain name should be lower cased'() {
        HttpServletRequest request = buildHttpServletRequest('http://M.pornhub.com/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(301)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    @Test
    void 'redirects should not copy data'() {
        HttpServletRequest request = buildHttpServletRequest('http://readcomics.net/images/site/front/bghead2.jpg')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(301)) },
            setHeader: { String name, String value -> assertThat(name, not(is('Accept-Ranges'))) },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    @Test(expected = NumberFormatException)
    void 'broken port values should return a 400 bad request response'() {
        HttpServletRequest request = buildHttpServletRequestFromRawURI('/rnw-https/business.us:a.gov/')
        proxy.proxy('http', request, null)
    }
    
    @Test
    void 'URLs with missing trailing slash should be redirected 1'() {
        HttpServletRequest request = buildHttpServletRequest('http://rah.com')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            sendRedirect: { String location -> assertThat(location, is('//fnuh.com/http/rah.com/')) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    @Test
    void 'URLs with missing trailing slash should be redirected 2'() {
        HttpServletRequest request = buildHttpServletRequest('http://rah.com?fnuh=guh')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            sendRedirect: { String location -> assertThat(location, is('//fnuh.com/http/rah.com/?fnuh=guh')) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    // TODO: https://static.tutsplus.com/assets/fontawesome-webfont-3cd310e486271a9d3d86b56ce2706de5.woff2?v=4.3.0 rewritten as text/html
    // TODO: http://tour.desipapa.com/fonts/glyphicons-halflings-regular.woff2 rewritten as text/html

}
