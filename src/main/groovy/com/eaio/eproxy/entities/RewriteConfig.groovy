package com.eaio.eproxy.entities

import groovy.transform.CompileStatic
import groovy.transform.Immutable

import com.eaio.eproxy.rewriting.*

/**
 * Configures rewriting in {@link Rewriting}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Immutable
class RewriteConfig {

    /**
     * Remove any active content.
     * 
     * @see {@link RemoveActiveContentFilter}
     */
    boolean removeActiveContent
    
    /**
     * Rewrite URLs to stay on the proxy.
     * 
     * @see {@link RewritingFilter}
     */
    boolean rewrite
    
    /**
     * Remove <tt>&lt;noscript&gt;</tt> elements.
     * 
     * @see {@link RemoveNoScriptElementsFilter}
     */
    boolean removeNoScriptElements
    
    static RewriteConfig fromString(String config) {
        new RewriteConfig(config?.contains('r'), config?.contains('w'), config?.contains('n'))
    }
    
    /**
     * @return whether any properties are <tt>true</tt>
     */
    boolean asBoolean() {
        removeActiveContent || rewrite || removeNoScriptElements
    }
    
    String toString() {
        [ removeActiveContent ? 'r' : '', removeNoScriptElements ? 'n' : '', rewrite ? 'w' : '', '-' ].join('')
    }

}
