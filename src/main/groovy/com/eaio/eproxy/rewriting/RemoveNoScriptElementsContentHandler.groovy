package com.eaio.eproxy.rewriting

import groovy.transform.InheritConstructors

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
@InheritConstructors
class RemoveNoScriptElementsContentHandler extends DelegatingContentHandler {

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'noscript')) {
            stack.push(name(localName, qName))
        }
        else if (!stack.contains('noscript')) {
            delegate.startElement(uri, localName, qName, atts)
        }
    }

    void endElement(String uri, String localName, String qName) throws SAXException {
        if (nameIs(localName, qName, 'noscript')) {
            try {
                stack.pop()
            }
            catch (EmptyStackException ex) {}
        }
        else if (!stack.contains('noscript')) {
            delegate.endElement(uri, localName, qName)
        }
    }

}
