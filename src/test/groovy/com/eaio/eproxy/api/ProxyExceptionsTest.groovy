package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.protocol.HttpContext
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.eproxy.api.Proxy.OutOfMemoryException
import com.eaio.eproxy.api.Proxy.PermissionDeniedException

/**
 * Tests exception handling.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class ProxyExceptionsTest {
    
    @Lazy
    Proxy proxy

    @Test
    @Parameters(method = 'throwables')
    void 'Proxy should handle thrown Throwable correctly'(Throwable thrw, Matcher<Throwable> expected) {
        proxy.httpClient =  [ execute: { HttpUriRequest request, HttpContext context -> throw thrw } ] as HttpClient
        try {
            proxy.proxy('rnw', 'http', buildHttpServletRequest('http://rah.com/'), null)
        }
        catch (Throwable t) {
            assertThat(t, expected)
        }
    }
    
    Collection<Object[]> throwables() {
        [
            [ new SocketException('Permission denied'), is(PermissionDeniedException) ],
            [ new OutOfMemoryError(), is(OutOfMemoryException) ],
        ].collect { it as Object[] }
    }

}
