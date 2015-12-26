package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*

import org.apache.xerces.parsers.AbstractSAXParser.AttributesProxy
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * Base class for SAX 2 {@link ContentHandler ContentHandlers}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
class BaseContentHandler extends DefaultHandler {
    
    final Stack<String> stack = new Stack<String>()

    String name(String localName, String qName) {
        lowerCase(defaultString(localName, defaultString(qName)))
    }

    boolean nameIs(String localName, String qName, String expected) {
        equalsIgnoreCase(name(localName, qName), expected)
    }
    
    void setAttributeValue(Attributes atts, int index, String value) {
        //((AttributesImpl) atts).setValue(index, value) // TagSoup
        ((AttributesProxy) atts).@fAttributes.setValue(index, value) // NekoHTML
    }
    
    void removeAttribute(Attributes atts, int index) {
        //((AttributesImpl) atts).removeAttribute(index) // TagSoup
        ((AttributesProxy) atts).@fAttributes.removeAttributeAt(index) // NekoHTML
    }

}
