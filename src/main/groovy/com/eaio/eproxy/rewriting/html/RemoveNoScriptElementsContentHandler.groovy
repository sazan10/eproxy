package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

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
@CompileStatic
class RemoveNoScriptElementsContentHandler extends DelegatingContentHandler {
    
    boolean inNoscriptBlock

    @Override
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'noscript')) {
            inNoscriptBlock = true
        }
        else if (!inNoscriptBlock) {
            documentHandler.startElement(uri, localName, qName, atts)
        }
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        if (nameIs(localName, qName, 'noscript')) {
            inNoscriptBlock = false
        }
        else if (!inNoscriptBlock) {
            documentHandler.endElement(uri, localName, qName)
        }
    }
    
    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        if (!inNoscriptBlock) {
            documentHandler.characters(ch, start, length)
        }
    }

}
