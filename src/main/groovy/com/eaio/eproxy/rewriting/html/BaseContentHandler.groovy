package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import static org.apache.commons.lang3.reflect.FieldUtils.*
import groovy.transform.CompileStatic

import org.apache.xerces.xni.XMLAttributes
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * Base class for SAX 2 {@link ContentHandler ContentHandlers}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
@CompileStatic
class BaseContentHandler extends DefaultHandler {
    
    final Stack<String> stack = new Stack<String>()

    String name(String localName, String qName) {
        lowerCase(defaultString(qName, localName))
    }

    boolean nameIs(String localName, String qName, String expected) {
        equalsIgnoreCase(name(localName, qName), expected)
    }
    
    void setAttributeValue(Attributes atts, int index, String value) {
        ((XMLAttributes) readDeclaredField(atts, 'fAttributes', true)).setValue(index, value)
    }
    
    void removeAttribute(Attributes atts, int index) {
        ((XMLAttributes) readDeclaredField(atts, 'fAttributes', true)).removeAttributeAt(index)
    }

}
