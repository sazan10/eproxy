package com.eaio.eproxy.entities

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RewriteConfigTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    /**
     * Test method for {@link com.eaio.eproxy.entities.RewriteConfig#asBoolean()}.
     */
    @Test
    void testAsBoolean_1() {
        assertThat(new RewriteConfig() as boolean, is(false))
    }
    
    /**
     * Test method for {@link com.eaio.eproxy.entities.RewriteConfig#asBoolean()}.
     */
    @Test
    void testAsBoolean_2() {
        assertThat(new RewriteConfig(removeActiveContent: true, rewrite: true, removeNoScriptElements: true) as boolean, is(true))
    }
    
    @Test
    void 'fromString should set parameters'() {
        RewriteConfig config = RewriteConfig.fromString('rn')
        errorCollector.checkThat(config.removeActiveContent, is(true))
        errorCollector.checkThat(config.removeNoScriptElements, is(true))
        errorCollector.checkThat(config.rewrite, is(false))
    }
    
    @Test
    void 'equals and hashCode'() {
        RewriteConfig config = RewriteConfig.fromString('rn')
        errorCollector.checkThat(config.equals(config), is(true))
        errorCollector.checkThat(config.equals(null), is(false))
        errorCollector.checkThat(config.hashCode(), is(config.hashCode()))
    }
    
    @Test
    void 'fromString should be null-safe'() {
        RewriteConfig config = RewriteConfig.fromString(null)
        errorCollector.checkThat(config.removeActiveContent, is(false))
        errorCollector.checkThat(config.removeNoScriptElements, is(false))
        errorCollector.checkThat(config.rewrite, is(false))
        errorCollector.checkThat(config.asBoolean(), is(false))
    }

}
