package com.eaio.eproxy.rewriting.css

import static org.apache.commons.lang3.StringUtils.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.commons.lang3.text.StrBuilder
import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import org.w3c.css.sac.InputSource
import org.xml.sax.XMLReader

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting
import com.eaio.net.httpclient.ReEncoding

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class CSSRewritingFilterTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Test
    void 'escaped CSS attributes should be rewritten'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newHTMLReader()
        XMLDocumentFilter[] filters = [ new CSSRewritingFilter(reEncoding: new ReEncoding(), baseURI: 'http://rah.com'.toURI(),
            requestURI: 'https://plop.com/ui.html?fnuh=guh'.toURI(), rewriteConfig: RewriteConfig.fromString('rnw')),
            new org.cyberneko.html.filters.Writer(output, 'UTF-8') ].toArray()
        xmlReader.setProperty('http://cyberneko.org/html/properties/filters', filters)
        xmlReader.parse(new org.xml.sax.InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        // Either rewrite or drop the escaped rules.
        errorCollector.checkThat(output as String, anyOf(containsString('url(http://rah.com/rnw-https/plop.com/bla.jpg'),
            not(containsString('bla.jpg'))))
        errorCollector.checkThat(output as String, anyOf(containsString('url(http://rah.com/rnw-https/plop.com/keks.jpg'),
            not(containsString('keks.jpg'))))
        errorCollector.checkThat(output as String, containsString('background-image: image(&quot;http://rah.com/rnw-http/creme.com/aha.jpg&quot;)'))
    }

    @Lazy
    CSSRewritingFilter cssRewritingFilter = new CSSRewritingFilter(reEncoding: new ReEncoding(), baseURI: 'http://fnuh.com/'.toURI(),
        requestURI: 'https://www.google.com/'.toURI(), rewriteConfig: RewriteConfig.fromString('rnw'))
    
    @Test
    void 'should rewrite inline style sheet'() {
        String style = 'height:110px;width:276px;background:url(/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png) no-repeat'
        Writer out = new StringWriter()
        cssRewritingFilter.rewriteStyleAttribute(new InputSource(characterStream: new StringReader(style)), out)
        assertThat(out as String,
            anyOf(containsString('background:url(http://fnuh.com/rnw-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png'),
                containsString('background: url(http://fnuh.com/rnw-https/www.google.com/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png')))
    }

    @Test
    @Parameters(method = 'cssFiles')
    void 'should rewrite CSS file'(File cssFile) {
        StrBuilder builder = new StrBuilder()
        cssRewritingFilter.rewriteCSS(new InputSource(characterStream: cssFile.newReader()), builder.asWriter())
        String rewritten = builder as String
        assertThat(cssFile.name, trimToNull(rewritten), notNullValue())
        assertThat(cssFile.name, rewritten, allOf(containsString('http://fnuh.com/rnw-'),
            not(containsString('/rnw-data')), not(containsString('url(/rsrc.php')), not(containsString('https://leaking.via'))))
    }

    Collection<Object[]> cssFiles() {
        new File('src/test/resources/com/eaio/eproxy/rewriting/css').listFiles().collect { [ it ] as Object[] }
    }

}
