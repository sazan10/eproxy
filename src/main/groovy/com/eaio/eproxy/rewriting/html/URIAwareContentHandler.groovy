package com.eaio.eproxy.rewriting.html

import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig

/**
 * {@link ContentHandler} that knows where the document is coming from.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URIAwareContentHandler extends RewritingContentHandler {

    URI baseURI, requestURI 
    
    RewriteConfig rewriteConfig
    
}
