package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.InheritConstructors

import org.ccil.cowan.tagsoup.AttributesImpl
import org.springframework.stereotype.Component
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException

/**
 * Transforms HTML as follows:
 * <ul>
 * <li>Any scripts are removed.
 * <li>Any on* handlers are removed.
 * </ul>
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: TryEaioTransformer.java 7547 2015-07-01 20:02:47Z johann $
 */
@Component
@InheritConstructors
class RemoveActiveContentDelegatingContentHandler extends DelegatingContentHandler {
    
    @Override
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // Skip scripts but add them to the stack.
        if (nameIs(localName, qName, 'script')) {
            stack.push(name(localName, qName))
        }
        else {
            for (int i = 0; i < atts.getLength(); ++i) {
                if (startsWithIgnoreCase(name(atts.getLocalName(i), atts.getQName(i)), 'on')) {
                    ((AttributesImpl) atts).removeAttribute(i)
                }
            }
            delegate.startElement(uri, localName, qName, atts)
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
            delegate.endElement(uri, localName, qName)
        }
    }

    /**
     * Skips <tt>&lt;script&gt;</tt> contents.
     * @see com.eaio.try_eaio.HTMLSerializer#characters(char[], int, int)
     */
    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        String tag = ''
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (!defaultString(tag).equals('script')) {
            delegate.characters(ch, start, length)
        }
    }

}
