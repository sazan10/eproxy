package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.lang.Lazy

import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation
import com.eaio.stringsearch.BNDMCI

/**
 * Rewrites <tt>src</tt>, <tt>href</tt> and <tt>action</tt> attributes.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class URIRewritingContentHandler extends URIAwareContentHandler {
    
    @Lazy
    private transient BNDMCI bndmci = new BNDMCI()
    
    @Lazy
    private transient Object patternHTTP = bndmci.processString('http:'),
        patternHTTPS = bndmci.processString('https:'),
        patternSlash = bndmci.processString(':/')

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        atts.length.times { int i ->
            String attributeValue = trimToEmpty(atts.getValue(i))
            
            // Use local name
            if (attributeNameShouldBeRewritten(atts.getLocalName(i)) && attributeValueMayNeedRewriting(attributeValue)) {
                setAttributeValue(atts, i, rewrite(baseURI, requestURI, attributeValue, rewriteConfig))
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }
    
    private boolean attributeValueMayNeedRewriting(String attributeValue) {
        attributeValue.startsWith('/') || bndmci.searchString(attributeValue, 'http:', patternHTTP) >= 0I || bndmci.searchString(attributeValue, 'https:', patternHTTPS) >= 0I || bndmci.searchString(attributeValue, ':/', patternSlash)
    }
    
    private boolean attributeNameShouldBeRewritten(String attributeName) {
        switch (attributeName) {
            case 'value':
            case 'object':
            case 'archive':
            case 'icon':
            case 'code':
            case 'codebase':
            case 'movie':
            case 'data':
            case 'poster':
            case 'formaction':
            case 'longdesc':
            case 'lowsrc':
            case 'dynsrc':
            case 'manifest':
            case 'implementation':
            case 'background':
            case 'href':
            case 'src':
            case 'action':
            case 'ping': return true
        }
        false
    }

}
