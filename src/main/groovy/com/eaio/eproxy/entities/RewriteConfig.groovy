package com.eaio.eproxy.entities

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Immutable
class RewriteConfig {

    boolean removeActiveContent, rewrite, removeNoScriptElements
    
    static RewriteConfig fromString(String config) {
        new RewriteConfig(config?.contains('r'), config?.contains('w'), config?.contains('n'))
    }
    
    boolean asBoolean() {
        removeActiveContent || rewrite || removeNoScriptElements
    }
    
    // Temporary
    String toString() {
        'ah-'
    }

}
