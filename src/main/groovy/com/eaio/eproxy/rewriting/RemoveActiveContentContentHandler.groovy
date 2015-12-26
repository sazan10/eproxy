package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*

import org.apache.xerces.parsers.AbstractSAXParser.AttributesProxy
import org.ccil.cowan.tagsoup.AttributesImpl
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
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
class RemoveActiveContentContentHandler extends DelegatingContentHandler {
    
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'script')) {
            stack.push('script')
        }
        else {
            for (int i = 0I; i < atts?.length; ) {
                if (startsWithIgnoreCase(name(atts.getLocalName(i), atts.getQName(i)), 'on')) {
                    //((AttributesImpl) atts).removeAttribute(i) // TagSoup
                    ((AttributesProxy) atts).@fAttributes.removeAttributeAt(i) // NekoHTML
                }
                else {
                    ++i
                }
            }
            delegate.startElement(uri, localName, qName, atts)
        }
    }

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
     */
    void characters(char[] ch, int start, int length) throws SAXException {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag != 'script') {
            delegate.characters(ch, start, length)
        }
    }

}
