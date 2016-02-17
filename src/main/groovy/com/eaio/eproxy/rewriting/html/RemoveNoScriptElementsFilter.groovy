package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString

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
class RemoveNoScriptElementsFilter extends BaseFilter {
    
    private boolean inNoscriptElement

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'noscript')) {
            inNoscriptElement = true
        }
        else if (!inNoscriptElement) {
            super.startElement(qName, atts, augs)
        }
    }

    @Override
    void endElement(QName qName, Augmentations augs) {
        if (nameIs(qName, 'noscript')) {
            inNoscriptElement = false
        }
        else if (!inNoscriptElement) {
            super.endElement(qName, augs)
        }
    }
    
    @Override
    void characters(XMLString xmlString, Augmentations augs) {
        if (!inNoscriptElement) {
            super.characters(xmlString, augs)
        }
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) {
        if (!inNoscriptElement) {
            super.emptyElement(element, attributes, augs)
        }
    }

}
