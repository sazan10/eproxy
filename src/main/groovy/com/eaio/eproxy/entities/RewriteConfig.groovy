package com.eaio.eproxy.entities

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RewriteConfig {

    boolean removeActiveContent, rewrite, cloakUserAgent, removeNoScriptElements
    
    boolean asBoolean() {
        removeActiveContent || rewrite || cloakUserAgent || removeNoScriptElements
    }

}
