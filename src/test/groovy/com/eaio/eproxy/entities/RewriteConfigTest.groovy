package com.eaio.eproxy.entities

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Test

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class RewriteConfigTest {

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
        assertThat(new RewriteConfig(removeActiveContent: true, rewrite: true, cloakUserAgent: true, removeNoScriptElements: true) as boolean, is(true))
    }

}
