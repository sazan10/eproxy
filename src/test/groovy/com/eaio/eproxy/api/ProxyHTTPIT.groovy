package com.eaio.eproxy.api

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import static com.eaio.eproxy.RequestMocks.*

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
import com.eaio.net.httpclient.ReEncoding

/**
 * Simulates enabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=1', randomPort = true)
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
    void 'range request should return only a part of the response'() {
        HttpServletRequest request = buildHttpServletRequest('http://bilder1.n-tv.de/img/incoming/crop16474236/4269152083-cImg_17_6-w680/imago66342948h.jpg', 'GET', { String name -> name == 'Range' ? 'bytes=0-99' : null })
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status -> assertThat(status, anyOf(is(200I), is(206I))) }, // setStatus(int) is called twice
            setHeader: { String name, String value -> if (name == 'Content-Range') { assertThat(value, is('bytes 0-99/48015')) } },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
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
        assertThat(bOut.toString(0I), containsString('FORM action="http://fnuh.com/rnw-https/www.google.com:443/search"'))
    }
    
}
