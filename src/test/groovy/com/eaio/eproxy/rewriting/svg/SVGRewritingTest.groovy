package com.eaio.eproxy.rewriting.svg

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import java.nio.charset.Charset

import org.junit.Test

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting

/**
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class SVGRewritingTest {
    
    @Lazy
    Rewriting rewriting

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
        rewriting.rewriteSVG(new File('src/test/resources/com/eaio/eproxy/rewriting/svg/mm-logo.svg').newInputStream(), bOut, Charset.forName('UTF-8'), URI.create('http://oha.com/ui/'), URI.create('http://fnuh.com/mm-logo.svg'), RewriteConfig.fromString('rnw'))
        assertThat(bOut as String, containsString('.st0{fill:#df0000}'))
    }

}
