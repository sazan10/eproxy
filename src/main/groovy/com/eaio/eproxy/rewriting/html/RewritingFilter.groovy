
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
        // Exclude HTML attribute values
        if (attributeValue && !attributeValue?.startsWith('<') && !attributeValue?.startsWith('#')) {
            int colonIndex = attributeValue.indexOf(':')
            if (colonIndex == -1I) {
                colonIndex = Integer.MAX_VALUE
            }
            if (attributeValue.startsWith('/')) {
                return true
            }
            int index
            index = bndmci.searchString(attributeValue, 'http:', patternHTTP)
            if (index >= 0I && index < colonIndex) {
                return true
            }
            index = bndmci.searchString(attributeValue, 'https:', patternHTTPS)
            if (index >= 0I && index < colonIndex) {
                return true
            }
            index = bndmci.searchString(attributeValue, ':/', patternColonSlash)
            if (index >= 0I && index < colonIndex) {
                return true
            }
            bndmci.searchString(attributeValue, 'view-source:', patternViewSource) == 0I
        }
    }

}
