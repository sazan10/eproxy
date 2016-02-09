package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.net.httpclient.ReEncoding
import com.eaio.stringsearch.BNDMCI

/**
 * {@link BaseFilter} that knows where the document is coming from and decides whether attribute values should be rewritten.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
class RewritingFilter extends BaseFilter {
    
    ReEncoding reEncoding

    URI baseURI, requestURI

    RewriteConfig rewriteConfig

    @Lazy
    private BNDMCI bndmci = new BNDMCI()

    @Lazy
    private def patternHTTP = bndmci.processString('http:'),
        patternHTTPS = bndmci.processString('https:'),
        patternColonSlash = bndmci.processString(':/'),
        patternViewSource = bndmci.processString('view-source:')

    boolean attributeValueNeedsRewriting(String attributeValue) {
        if (attributeValue) {
            int colonIndex = attributeValue.indexOf((int) ((char) ':'))
                    if (colonIndex == -1I) {
                        colonIndex = Integer.MAX_VALUE
                    }
            [
                { attributeValue.startsWith('/') },
                { int index = bndmci.searchString(attributeValue, 'http:', patternHTTP); index >= 0I && index < colonIndex },
                { int index = bndmci.searchString(attributeValue, 'https:', patternHTTPS); index >= 0I && index < colonIndex },
                { int index = bndmci.searchString(attributeValue, ':/', patternColonSlash); index >= 0I && index < colonIndex },
                { bndmci.searchString(attributeValue, 'view-source:', patternViewSource) == 0I },
            ].any { it() }
        }
    }

}
