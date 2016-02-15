package com.eaio.eproxy.rewriting.svg

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import java.nio.charset.Charset

import org.junit.Test

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting

class SVGRewritingTest {
    
    @Lazy
    Rewriting rewriting

    @Test
    void testIsSVG() {
        assertThat(rewriting.isSVG('IMAGE/SVG+XML'), is(true))
    }

    @Test
    void testRewriteSVG() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        rewriting.rewriteSVG(new File('src/test/resources/com/eaio/eproxy/rewriting/svg/mm-logo.svg').newInputStream(), bOut, Charset.forName('UTF-8'), URI.create('http://oha.com/ui/'), URI.create('http://fnuh.com/mm-logo.svg'), RewriteConfig.fromString('rnw'))
        assertThat(bOut as String, containsString('.st0{fill:#df0000}'))
    }

}
