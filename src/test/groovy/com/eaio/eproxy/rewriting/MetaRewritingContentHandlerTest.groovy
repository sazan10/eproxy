package com.eaio.eproxy.rewriting

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class MetaRewritingContentHandlerTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()
    
    @Lazy
    MetaRewritingContentHandler contentHandler

    /**
     * Test method for {@link com.eaio.eproxy.rewriting.MetaRewritingContentHandler#rewriteRefresh(java.lang.String)}.
     */
    @Test
    void testRewriteRefresh() {
        errorCollector.checkThat(contentHandler.getURL('0; URL=/?_rdr=p&_fb_noscript=1'), is('/?_rdr=p&_fb_noscript=1'))
        errorCollector.checkThat(contentHandler.getURL('0 ; url=/?_rdr=p&_fb_noscript=1'), is('/?_rdr=p&_fb_noscript=1'))
    }

}
