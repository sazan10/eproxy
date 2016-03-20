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
@WebIntegrationTest(value = [ 'http.maxRedirects=1', 'cookies.enabled=false' ], randomPort = true)
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
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toString(0I), containsString('johannburkard/eproxy'))
    }
    
    @Test
    void 'range requests should return only a part of the response'() {
        HttpServletRequest request = buildHttpServletRequest('http://bilder1.n-tv.de/img/incoming/crop16474236/4269152083-cImg_17_6-w680/imago66342948h.jpg', 'GET', { String name -> name == 'Range' ? 'bytes=0-99' : null })
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(200I), is(206I))) }, // setStatus(int) is called twice
            setHeader: { String name, String value -> if (name == 'Content-Range') { assertThat(value, is('bytes 0-99/48015')) } },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
            setContentLength: { int length -> assertThat(length, is(100I)) },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'http', request, response)
        assertThat(bOut.toByteArray().encodeBase64() as String, is('/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDA=='))
    }
    
    @Test
    void 'ports should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.google.com:443/')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('rnw', 'https', request, response)
        assertThat(bOut.toString(0I), containsString('form action="http://fnuh.com/rnw-https/www.google.com:443/search"'))
    }
    
    // Test URLs from media.io, may still be broken or not
    
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
    void 'broken HTTPS should be supported 3'() {
        HttpServletRequest request = buildHttpServletRequest('https://t.co/0fRMkR6AOo')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('https', request, response)
        assertThat(bOut.toString(0I), containsString('The Red Line'))
    }
    
    @Test
    void 'broken redirects should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('http://bit.ly/19xbm5w')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(bOut.toString(0I), containsString('Star Wars'))
    }
    
    @Test
    void 'non-existing domains should return a 404'() {
        HttpServletRequest request = buildHttpServletRequest('http://das-ist-absolut-cla.com')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            sendError: { int status, String sc -> assertThat(status, is(404I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    @Test
    void 'invalid range requests should return a 416'() {
        HttpServletRequest request = buildHttpServletRequest('http://eaio.com/robots.txt', 'GET', { String name -> name == 'Range' ? 'bytes=5000-' : null })
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        int calls = 0I
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) }, // status is first set to 200...
            sendError: { int status -> assertThat(status, is(416I)) }, // ...then to 416
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }

    @Test
    void 'trace requests should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('http://repo.eaio.com', 'TRACE')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(405I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
    }
    
    @Test
    void 'uppercase protocol should be supported'() {
        HttpServletRequest request = buildHttpServletRequest('HTTP://www.n-tv.de')
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, is(200I)) },
            setHeader: { String name, String value -> },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('HTTP', request, response)
        assertThat(bOut.toString(0I), containsString('Nachrichten'))
    }

}
