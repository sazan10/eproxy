package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*

import groovy.transform.TupleConstructor

import org.xml.sax.ContentHandler
import org.xml.sax.Locator

/**
 * SAX 2 {@link ContentHandler} that delegates to another {@link ContentHandler}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
class DelegatingContentHandler {
    
    final Stack<String> stack = new Stack<String>()

    Locator documentLocator

    @Delegate    
    ContentHandler delegate

    @Override
    void setDocumentLocator(Locator documentLocator) {
        this.documentLocator = documentLocator
        delegate?.setDocumentLocator(documentLocator)
    }
    
    String name(String localName, String qName) {
        lowerCase(defaultString(localName, defaultString(qName)))
    }

    boolean nameIs(String localName, String qName, String expected) {
        equalsIgnoreCase(name(localName, qName), expected)
    }

}
