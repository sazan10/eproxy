package com.eaio.eproxy.rewriting.html

import com.eaio.eproxy.entities.RewriteConfig

/**
 * {@link ContentHandler} that knows where the document is coming from.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URIAwareContentHandler extends DelegatingContentHandler {

    URI baseURI, requestURI 
    
    RewriteConfig rewriteConfig
    
}
