package com.eaio.eproxy.rewriting.css

import static org.apache.commons.lang3.StringUtils.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import org.xml.sax.XMLReader

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class CSSRewritingFilterTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Lazy
    CSSRewritingFilter cssRewritingFilter = new CSSRewritingFilter(baseURI: 'http://fnuh.com/'.toURI(),
        requestURI: 'https://www.google.com/'.toURI(), rewriteConfig: RewriteConfig.fromString('rnw'))
    
    @Test
    void 'escaped CSS attributes should be rewritten'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newHTMLReader()
        XMLDocumentFilter[] filters = [ cssRewritingFilter, new org.cyberneko.html.filters.Writer(output, 'UTF-8') ].toArray()
        xmlReader.setProperty('http://cyberneko.org/html/properties/filters', filters)
        xmlReader.parse(new org.xml.sax.InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        // Either rewrite or drop the escaped rules.
        errorCollector.checkThat(output as String, containsString('http://fnuh.com/rnw-https/www.google.com/keks.jpg'))
        errorCollector.checkThat(output as String, anyOf(
            containsString('background-image: image(http://fnuh.com/rnw-http/creme.com/aha.jpg)'),
            containsString('background-image: image(\'http://fnuh.com/rnw-http/creme.com/aha.jpg\')')))
        errorCollector.checkThat(output as String, containsString('background-image: \\75\\72\\6C\\28http://fnuh.com/rnw-https/www.google.com/bla.jpg\\29'))
    }

    @Test
    void 'should rewrite inline style sheet'() {
        String style = 'height:110px;width:276px;background:url(/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png) no-repeat'
        String out = cssRewritingFilter.rewriteCSS(style)
        assertThat(out,
            anyOf(containsString('background:url(http://fnuh.com/rnw-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png'),
                containsString('background: url(http://fnuh.com/rnw-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png')))
    }

    @Test
    @Parameters(method = 'cssFiles')
    void 'should rewrite CSS file'(File cssFile) {
        String rewritten = cssRewritingFilter.rewriteCSS(cssFile.text)
        errorCollector.checkThat(cssFile.name, trimToNull(rewritten), notNullValue())
        errorCollector.checkThat(cssFile.name, rewritten, allOf(containsString('http://fnuh.com/rnw-'),
            not(containsString('/rnw-data:')), not(containsString('url(/rsrc.php')), not(containsString('//leaking.via'))))
    }

    Collection<Object[]> cssFiles() {
        new File('src/test/resources/com/eaio/eproxy/rewriting/css').listFiles().collect { [ it ] as Object[] }
    }
    
    @Test
    void 'should not write beyond src attribute'() {
        String style = '''.fancybox-ie6 #fancybox-left-ico{background:transparent;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/images/fancybox/fancy_nav_left-333220e.png', sizingMethod='scale')}'''
        String out = cssRewritingFilter.rewriteCSS(style)
        assertThat(out, containsString(", sizingMethod='scale'"))
    }
    
    @Test
    void 'should not remove @import rules'() {
        String rewritten = cssRewritingFilter.rewriteCSS(new File('src/test/resources/com/eaio/eproxy/rewriting/css/www.google.com_intl_en_ads_css_default.v3.css').text)
        assertThat(rewritten, containsString('base.css'))
    }
    
}
