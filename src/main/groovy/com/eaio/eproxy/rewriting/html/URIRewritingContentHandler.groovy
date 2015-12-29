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
        atts?.length?.times { int i ->
            String attributeName = name(atts.getLocalName(i), atts.getQName(i)), attributeValue = trimToEmpty(atts.getValue(i))
            
            if ((equalsIgnoreCase(attributeName, 'href') || equalsIgnoreCase(attributeName, 'src') || equalsIgnoreCase(attributeName, 'action')) &&
                (attributeValue.startsWith('/') || startsWithIgnoreCase(attributeValue, 'http:') || startsWithIgnoreCase(attributeValue, 'https:'))) {
                
                setAttributeValue(atts, i, rewrite(baseURI, requestURI, attributeValue, rewriteConfig))
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }

}
