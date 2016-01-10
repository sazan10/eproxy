package com.eaio.eproxy.rewriting.html

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.net.httpclient.ReEncoding
import com.eaio.stringsearch.BNDMCI

/**
 * {@link ContentHandler} that knows where the document is coming from and decides whether attribute values should be rewritten.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RewritingContentHandler extends DelegatingContentHandler {
    
    ReEncoding reEncoding

    URI baseURI, requestURI

    RewriteConfig rewriteConfig

    @Lazy
    private BNDMCI bndmci = new BNDMCI()

    @Lazy
    private def patternHTTP = bndmci.processString('http:'),
        patternHTTPS = bndmci.processString('https:'),
        patternColonSlash = bndmci.processString(':/')

    // TODO: data: and javascript: URIs
    boolean attributeValueNeedsRewriting(String attributeValue) {
        attributeValue.startsWith('/') ||
            bndmci.searchString(attributeValue, 'http:', patternHTTP) >= 0I ||
            bndmci.searchString(attributeValue, 'https:', patternHTTPS) >= 0I ||
            bndmci.searchString(attributeValue, ':/', patternColonSlash) >= 0I
    }

}
