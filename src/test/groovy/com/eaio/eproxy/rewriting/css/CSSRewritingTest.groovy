package com.eaio.eproxy.rewriting.css

import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.URLManipulation
import com.helger.css.decl.*

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
@RunWith(JUnitParamsRunner)
class CSSRewritingTest {
    
    @Lazy
    CSSRewritingContentHandler cssRewritingContentHandler = new CSSRewritingContentHandler(baseURI: 'http://fnuh.com/'.toURI(),
        requestURI: 'https://www.google.com/'.toURI(), rewriteConfig: new RewriteConfig(rewrite: true))

    @Test
    @Parameters(method = 'cssFilePaths')
    void 'should rewrite CSS file'(String cssFilePath) {
        String rewrittenCSS = cssRewritingContentHandler.rewriteCSS(new File(cssFilePath).newReader())
        println rewrittenCSS
    }
    
    Collection<Object[]> cssFilePaths() {
        [
            [ 'src/test/resources/com/eaio/eproxy/rewriting/css/static.xx.fbcdn.net_rsrc.php_v2_yp_r_I5kTXq1bSJZ.css' ],
            [ 'src/test/resources/com/eaio/eproxy/rewriting/css/www_google_de.css' ],
        ].collect { it as Object[] }
    }

}
