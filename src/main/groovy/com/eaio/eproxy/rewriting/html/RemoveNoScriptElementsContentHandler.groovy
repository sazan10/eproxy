package com.eaio.eproxy.rewriting.html

import org.xml.sax.Attributes
import org.xml.sax.SAXException

/**
 * Transforms HTML as follows:
 * <ul>
 * <li>Any noscript elements and their child nodes are removed.
 * </ul>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RemoveNoScriptElementsContentHandler extends DelegatingContentHandler {
    
    boolean inNoscriptBlock

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'noscript')) {
            inNoscriptBlock = true
        }
        else if (!inNoscriptBlock) {
            delegate.startElement(uri, localName, qName, atts)
        }
    }

    void endElement(String uri, String localName, String qName) throws SAXException {
        if (nameIs(localName, qName, 'noscript')) {
            inNoscriptBlock = false
        }
        else if (!inNoscriptBlock) {
            delegate.endElement(uri, localName, qName)
        }
    }
    
    void characters(char[] ch, int start, int length) throws SAXException {
        if (!inNoscriptBlock) {
            delegate.characters(ch, start, length)
        }
    }

}
