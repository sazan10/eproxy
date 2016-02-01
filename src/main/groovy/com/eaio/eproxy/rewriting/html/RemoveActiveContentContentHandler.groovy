package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.xml.sax.Attributes
import org.xml.sax.SAXException

/**
 * Transforms HTML as follows:
 * <ul>
 * <li>Any script elements are removed.
 * <li>Any on* handlers are removed.
 * </ul>
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: TryEaioTransformer.java 7547 2015-07-01 20:02:47Z johann $
 */
@CompileStatic
class RemoveActiveContentContentHandler extends DelegatingContentHandler {
    
    @Override
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'script')) {
            stack.push('script')
        }
        else {
            for (int i = 0I; i < atts.length; ) {
                if (startsWithIgnoreCase(name(atts.getLocalName(i), atts.getQName(i)), 'on')) {
                    removeAttribute(atts, i)
                }
                else {
                    ++i
                }
            }
            documentHandler.startElement(uri, localName, qName, atts)
        }
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        if (nameIs(localName, qName, 'script')) {
            try {
                stack.pop()
            }
            catch (EmptyStackException ex) {}
        }
        else {
            documentHandler.endElement(uri, localName, qName)
        }
    }

    /**
     * Skips <tt>&lt;script&gt;</tt> contents.
     */
    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag != 'script') {
            documentHandler.characters(ch, start, length)
        }
    }

}
