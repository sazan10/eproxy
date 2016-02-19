package com.eaio.eproxy.rewriting.html

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class RewritingFilterTest {
    
    @Lazy
    RewritingFilter rewritingFilter

    @Test
    @Parameters(method = 'attributeValues')
    void testAttributeValueNeedsRewriting(String value, boolean expectedRewriting) {
        assertThat(rewritingFilter.attributeValueNeedsRewriting(value), is(expectedRewriting))
    }
    
    Collection<Object[]> attributeValues() {
        [
            [ 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7', false ],
            [ 'DATA:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7', false ],
            [ 'javascript:location.href="http://foo.com"; return false;', false ],
            [ 'http://foo.com', true ],
            [ 'HTTPS://foo.com', true ],
            [ '/foo.html', true ],
        ].collect { it as Object[] }
    }

}
