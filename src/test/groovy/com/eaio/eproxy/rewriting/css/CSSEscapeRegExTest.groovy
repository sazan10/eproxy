package com.eaio.eproxy.rewriting.css

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import java.util.regex.Pattern

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class CSSEscapeRegExTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    /**
     * Test method for {@link com.eaio.eproxy.rewriting.css.CSSEscapeRegEx#appendPattern(java.lang.Appendable, java.lang.Object)}.
     */
    @Test
    void 'toPattern should be compilable'() {
        String regexp = CSSEscapeRegEx.toPattern('é'), expected = '(?:\\\\0{0,4}e9[ \\t\\n]?|é)'
        errorCollector.checkThat("${URLEncoder.encode(regexp)} should be ${URLEncoder.encode(expected)}", regexp, is(expected))
        errorCollector.checkThat(Pattern.compile(regexp), isA(Pattern))
        errorCollector.checkThat(Pattern.compile(regexp).matcher('é').matches(), is(true))
        errorCollector.checkThat(Pattern.compile(regexp).matcher('\\e9\n').matches(), is(true))
        errorCollector.checkThat(Pattern.compile(regexp).matcher('\\0e9 ').matches(), is(true))
        errorCollector.checkThat(Pattern.compile(regexp).matcher('\\00e9').matches(), is(true))
        errorCollector.checkThat(Pattern.compile(regexp).matcher('\\000e9').matches(), is(true))
        errorCollector.checkThat(Pattern.compile(regexp).matcher('\\0000e9').matches(), is(true))
    }

}
