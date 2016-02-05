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
    
    boolean inNoscriptBlock

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'noscript')) {
            inNoscriptBlock = true
        }
        else if (!inNoscriptBlock) {
            super.startElement(qName, atts, augs)
        }
    }

    @Override
    void endElement(QName qName, Augmentations augs) {
        if (nameIs(qName, 'noscript')) {
            inNoscriptBlock = false
        }
        else if (!inNoscriptBlock) {
            super.endElement(qName, augs)
        }
    }
    
    @Override
    void characters(XMLString xmlString, Augmentations augs) {
        if (!inNoscriptBlock) {
            super.characters(xmlString, augs)
        }
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) {
        if (!inNoscriptBlock) {
            super.emptyElement(element, attributes, augs)
        }
    }

}
