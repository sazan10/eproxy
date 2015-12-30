package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*

import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation

/**
 * Rewrites <tt>src</tt>, <tt>href</tt> and <tt>action</tt> attributes.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class URIRewritingContentHandler extends URIAwareContentHandler {

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        atts.length.times { int i ->
            String attributeValue = trimToEmpty(atts.getValue(i))
            
            if (attributeNameShouldBeRewritten(atts.getLocalName(i)) && // Use local name
                (attributeValue.startsWith('/') || startsWithIgnoreCase(attributeValue, 'http:') || startsWithIgnoreCase(attributeValue, 'https:'))) {
                
                setAttributeValue(atts, i, rewrite(baseURI, requestURI, attributeValue, rewriteConfig))
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }
    
    private boolean attributeNameShouldBeRewritten(String attributeName) {
        switch (attributeName) {
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
