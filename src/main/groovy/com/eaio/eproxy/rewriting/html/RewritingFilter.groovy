package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*

import groovy.transform.CompileStatic

import com.eaio.eproxy.entities.RewriteConfig

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

    boolean attributeValueNeedsRewriting(String attributeValue) {
        // Exclude HTML attribute values and anchor links
        String value = trimToNull(attributeValue)
        value != null && !value.startsWith('<') && !value.startsWith('#') &&
            (startsWithSlash(value) || startsWithHTTP(value) || startsWithHTTPS(value) || startsWithColonSlash(value) || startsWithViewSource(value))
    }
    
    private boolean startsWithSlash(String value) {
        value.startsWith('/')
    }

    private boolean startsWithHTTP(String value) {
        startsWithIgnoreCase(value, 'http:')
    }
    
    private boolean startsWithHTTPS(String value) {
        startsWithIgnoreCase(value, 'https:')
    }
    
    private boolean startsWithColonSlash(String value) {
        startsWith(value, ':/')
    }
    
    private boolean startsWithViewSource(String value) {
        startsWithIgnoreCase(value, 'view-source:')
    }

}
