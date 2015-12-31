package com.eaio.eproxy.rewriting.css

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.commons.lang3.text.StrBuilder
import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.URLManipulation

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
@RunWith(JUnitParamsRunner)
class CSSRewritingContentHandlerTest {
    
    @Lazy
    CSSRewritingContentHandler cssRewritingContentHandler = new CSSRewritingContentHandler(baseURI: 'http://fnuh.com/'.toURI(),
        requestURI: 'https://www.google.com/'.toURI(), rewriteConfig: new RewriteConfig(rewrite: true))

    @Test
    @Parameters(method = 'cssFilePaths')
    void 'should rewrite CSS file'(File cssFile) {
        StrBuilder builder = new StrBuilder()
        cssRewritingContentHandler.rewriteCSS(cssFile.newReader(), builder.asWriter())
        assertThat(builder.toString(), allOf(containsString('http://fnuh.com/ah-'), not(containsString('/ah-data')), not(containsString('url(/rsrc.php'))))
    }
    
    Collection<Object[]> cssFilePaths() {
        new File('src/test/resources/com/eaio/eproxy/rewriting/css').listFiles().collect { [ it ] as Object[] } 
    }
    
    @Test
    void 'should rewrite inline style sheet'() {
        String style = 'height:110px;width:276px;background:url(/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png) no-repeat'
        assertThat(cssRewritingContentHandler.rewriteStyleAttribute(style), containsString('background:url(http://fnuh.com/ah-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png'))
    }

}
