package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.stringsearch.BNDMCI

/**
 * {@link BaseFilter} that knows where the document is coming from and decides whether attribute values should be rewritten.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
class RewritingFilter extends BaseFilter {

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
        // Exclude HTML attribute values and anchor links
        if (attributeValue && !attributeValue?.startsWith('<') && !attributeValue?.startsWith('#')) {
            int colonIndex = attributeValue.indexOf(':')
            if (colonIndex == -1I) {
                colonIndex = Integer.MAX_VALUE
            }
            attributeValue.startsWith('/') ||
                containsHTTP(attributeValue, colonIndex) || 
                containsHTTPS(attributeValue, colonIndex) ||
                containsColonSlash(attributeValue, colonIndex) ||
                startsWithViewSource(attributeValue)
        }
    }

    private boolean containsHTTP(String attributeValue, int colonIndex) {
        int index = bndmci.searchString(attributeValue, 'http:', patternHTTP)
        index >= 0I && index < colonIndex
    }
    
    private boolean containsHTTPS(String attributeValue, int colonIndex) {
        int index = bndmci.searchString(attributeValue, 'https:', patternHTTPS)
        index >= 0I && index < colonIndex
    }
    
    private boolean containsColonSlash(String attributeValue, int colonIndex) {
        int index = bndmci.searchString(attributeValue, ':/', patternColonSlash)
        index >= 0I && index < colonIndex
    }
    
    private boolean startsWithViewSource(String attributeValue) {
        bndmci.searchString(attributeValue, 'view-source:', patternViewSource) == 0I
    }

}
