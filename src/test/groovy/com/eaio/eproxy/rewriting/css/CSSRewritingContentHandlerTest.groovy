package com.eaio.eproxy.rewriting.css

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.commons.lang3.text.StrBuilder
import org.junit.Test
import org.junit.runner.RunWith

import com.eaio.eproxy.entities.RewriteConfig

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class CSSRewritingContentHandlerTest {

    @Lazy
    CSSRewritingContentHandler cssRewritingContentHandler = new CSSRewritingContentHandler(baseURI: 'http://fnuh.com/'.toURI(),
        requestURI: 'https://www.google.com/'.toURI(), rewriteConfig: new RewriteConfig(rewrite: true))
    
    @Test
    void 'should rewrite inline style sheet'() {
        String style = 'height:110px;width:276px;background:url(/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png) no-repeat'
                assertThat(cssRewritingContentHandler.rewriteStyleAttribute(style),
                    anyOf(containsString('background:url(http://fnuh.com/ah-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png'),
                        containsString('background: url(http://fnuh.com/ah-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png')))
    }

    @Test
    @Parameters(method = 'cssFiles')
    void 'should rewrite CSS file'(File cssFile) {
        StrBuilder builder = new StrBuilder()
        cssRewritingContentHandler.rewriteCSS(cssFile.newReader(), builder.asWriter())
        String rewritten = builder as String
        assertThat(cssFile.name, trimToNull(rewritten), notNullValue())
        assertThat(cssFile.name, rewritten, allOf(containsString('http://fnuh.com/ah-'),
            not(containsString('/ah-data')), not(containsString('url(/rsrc.php')), not(containsString('https://leaking.via'))))
    }

    Collection<Object[]> cssFiles() {
//        [ [ new File('src/test/resources/com/eaio/eproxy/rewriting/css/z-ecx.images-amazon.com_images_G_01_browser-scripts_dpMergedOverallCSS_dpMergedOverallCSS-13821758196._V1_.css') ] ].collect { it as Object[] }
        new File('src/test/resources/com/eaio/eproxy/rewriting/css').listFiles().collect { [ it ] as Object[] }
    }

}
