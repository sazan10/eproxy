package com.eaio.eproxy.rewriting

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import java.nio.charset.Charset

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.Eproxy
import com.eaio.eproxy.entities.RewriteConfig

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(randomPort = true)
class RewritingIT {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Autowired
    Rewriting rewriting
    
    @Test
    void 'should rewrite CSS in SVG'() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteSVG(new File('src/test/resources/com/eaio/eproxy/rewriting/svg/style.svg').newInputStream(),
            bOut, null, 'http://lol.lol'.toURI(), 'http://blorb.com'.toURI(), RewriteConfig.fromString('rnw'))
        errorCollector.checkThat(bOut.toString(0I), containsString('url(//lol.lol/rnw-http/blorb.com/fnuh.png)'))
        errorCollector.checkThat(bOut.toString(0I), containsString('url(//lol.lol/rnw-http/keks.org/plop.png)'))
    }
    
    @Test
    void 'should rewrite external stylesheet'() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteSVG(new File('src/test/resources/com/eaio/eproxy/rewriting/svg/external-style-sheet.svg').newInputStream(),
            bOut, null, 'http://lol.lol'.toURI(), 'http://blorb.com'.toURI(), RewriteConfig.fromString('rnw'))
        errorCollector.checkThat(bOut.toString(0I), containsString('href="//lol.lol/rnw-http/blorb.com/svg-stylesheet.css"'))
    }
    
    @Test
    void 'SVG MIME type should be supported'() {
        assertThat(rewriting.isSVG('IMAGE/SVG+XML'), is(true))
    }
    
    @Test
    void 'canRewrite should support SVG'() {
        assertThat(rewriting.canRewrite(null, RewriteConfig.fromString('rnw'), 'image/svg+xml'), is(true))
    }

    @Test
    void 'SVG rewriting should not change whitespace'() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteSVG(new File('src/test/resources/com/eaio/eproxy/rewriting/svg/mm-logo.svg').newInputStream(), bOut,
            Charset.forName('UTF-8'), URI.create('http://oha.com/ui/'), URI.create('http://fnuh.com/mm-logo.svg'), RewriteConfig.fromString('rnw'))
        assertThat(bOut.toString(0I), containsString('.st0{fill:#df0000}'))
    }
    
    @Test
    void 'HTML fragment rewriting should be supported'() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteHTMLFragment(new ByteArrayInputStream('<a href="http://pruh.com">Mega-pruh!</a><b style="background-image: url(http://rah.com/ouch.png)">wah</b>'.bytes), bOut,
            Charset.forName('UTF-8'), URI.create('http://oha.com/ui/'), URI.create('http://fnuh.com/mm-logo.svg'), RewriteConfig.fromString('rnw'))
        assertThat(bOut.toString(0I), containsString('//oha.com/ui/rnw-http/pruh.com'))
        assertThat(bOut.toString(0I), containsString('url(//oha.com/ui/rnw-http/rah.com/ouch.png)'))
    }
    
    @Test
    void 'iframe srcdoc attributes should be rewritten'() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteHTML(new File('src/test/resources/com/eaio/eproxy/rewriting/html/iframe-srcdoc.html').newInputStream(), bOut, null, 'http://foo.com'.toURI(), 'http://repo.eaio.com/leak.html'.toURI(), RewriteConfig.fromString('rnw'))
        assertThat(bOut as String, containsString('//foo.com/rnw-http/rah.com/fnuh.jpg'))
    }
    
    @Test
    void 'should remove active content in SVG'() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteSVG(new File('src/test/resources/com/eaio/eproxy/rewriting/svg/locky.svg').newInputStream(),
            bOut, null, 'http://lol.lol'.toURI(), 'http://blorb.com'.toURI(), RewriteConfig.fromString('rnw'))
        assertThat(bOut.toString(0I), not(containsString('<script')))
    }
    
}
