package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString

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
class RemoveActiveContentFilter extends BaseFilter {

    private final Stack<String> stack = new Stack<String>()

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'script')) {
            stack.push('script')
        }
        else {
            for (int i = 0I; i < atts.length; ) {
                if (startsWithIgnoreCase(atts.getLocalName(i) ?: atts.getQName(i), 'on')) {
                    atts.removeAttributeAt(i)
                }
                else {
                    ++i
                }
            }
            super.startElement(qName, atts, augs)
        }
    }

    @Override
    void endElement(QName qName, Augmentations augs) {
        if (nameIs(qName, 'script')) {
            try {
                stack.pop()
            }
            catch (EmptyStackException ex) {}
        }
        else {
            super.endElement(qName, augs)
        }
    }

    /**
     * Skips <tt>&lt;script&gt;</tt> contents.
     */
    @Override
    void characters(XMLString xmlString, Augmentations augs) {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag != 'script') {
            super.characters(xmlString, augs)
        }
    }

}
