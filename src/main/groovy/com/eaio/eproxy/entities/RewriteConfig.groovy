package com.eaio.eproxy.entities

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RewriteConfig {

    boolean removeActiveContent, rewrite, cloakUserAgent
    
    boolean asBoolean() {
        !removeActiveContent && !rewrite && !cloakUserAgent
    }

}
