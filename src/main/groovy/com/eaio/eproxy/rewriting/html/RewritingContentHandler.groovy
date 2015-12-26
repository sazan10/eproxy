package com.eaio.eproxy.rewriting.html

import org.springframework.web.util.UriComponentsBuilder
import com.eaio.eproxy.rewriting.URLManipulation

import com.eaio.eproxy.entities.RewriteConfig

/**
 * {@link ContentHandler} that has a few URL rewriting methods.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 * @deprecated
 */
@Deprecated
@Mixin(URLManipulation)
class RewritingContentHandler extends DelegatingContentHandler {
    
}
