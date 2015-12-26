package com.eaio.eproxy.rewriting.html

import groovy.util.logging.Slf4j

import org.xml.sax.Attributes
import org.xml.sax.SAXException

/**
 * Rewrites URLs in embedded CSS.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Slf4j
class CSSRewritingContentHandler extends DelegatingContentHandler {
    
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'style')) {
            stack.push('style')
        }
        delegate.startElement(uri, localName, qName, atts)
    }

    void endElement(String uri, String localName, String qName) throws SAXException {
        if (nameIs(localName, qName, 'style')) {
            try {
                stack.pop()
            }
            catch (EmptyStackException ex) {}
        }
        delegate.endElement(uri, localName, qName)
    }

    /**
     * Rewrites <tt>&lt;style&gt;</tt> contents.
     */
    void characters(char[] ch, int start, int length) throws SAXException {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag == 'style') {
            //log.info('characters: {}', new String(ch, start, length))
        }
        delegate.characters(ch, start, length)
    }

}
