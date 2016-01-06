package com.eaio.eproxy.rewriting.html

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.stringsearch.BNDMCI

/**
 * {@link ContentHandler} that knows where the document is coming from.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URIAwareContentHandler extends DelegatingContentHandler {

    URI baseURI, requestURI

    RewriteConfig rewriteConfig

    @Lazy
    private BNDMCI bndmci = new BNDMCI()

    @Lazy
    private Object patternHTTP = bndmci.processString('http:'),
        patternHTTPS = bndmci.processString('https:'),
        patternSlash = bndmci.processString(':/')

    boolean attributeValueNeedsRewriting(String attributeValue) {
        attributeValue.startsWith('/') || bndmci.searchString(attributeValue, 'http:', patternHTTP) >= 0I || bndmci.searchString(attributeValue, 'https:', patternHTTPS) >= 0I || bndmci.searchString(attributeValue, ':/', patternSlash)
    }

}
