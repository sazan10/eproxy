package com.eaio.eproxy.rewriting.css

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class CSSUnescaperTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()
    
    @Lazy
    CSSUnescaper cssUnescaper

    /**
     * Test method for {@link org.apache.commons.lang3.text.translate.CharSequenceTranslator#translate(java.lang.CharSequence)}.
     */
    @Test
    void 'should unescape CSS'() {
        errorCollector.checkThat(cssUnescaper.translate('\\2b'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\02b'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\002b'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\0002b'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\00002b'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\2b '), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\2b\n'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\2b\t'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('fnuh\\2bguh'), is('fnuh+guh'))
        errorCollector.checkThat(cssUnescaper.translate('jah\\2b ah'), is('jah+ah'))
        errorCollector.checkThat(cssUnescaper.translate('jah\\ ah'), is('jah ah'))
        errorCollector.checkThat(cssUnescaper.translate('\\31\ta\\h'), is('1ah'))
        errorCollector.checkThat(cssUnescaper.translate('\\+'), is('+'))
        errorCollector.checkThat(cssUnescaper.translate('\\:'), is(':'))
        errorCollector.checkThat(cssUnescaper.translate('ah\\  rah'), is('ah  rah'))
        errorCollector.checkThat(cssUnescaper.translate('\\000075 \\00072\\006C'), is('url'))
    }

}
