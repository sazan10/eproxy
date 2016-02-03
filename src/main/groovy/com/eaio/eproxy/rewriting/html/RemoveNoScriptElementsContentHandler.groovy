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
class RemoveNoScriptElementsContentHandler extends BaseContentHandler {
    
    boolean inNoscriptBlock

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'noscript')) {
            inNoscriptBlock = true
        }
        else if (!inNoscriptBlock) {
            documentHandler.startElement(qName, atts, augs)
        }
    }

    @Override
    void endElement(QName qName, Augmentations augs) {
        if (nameIs(qName, 'noscript')) {
            inNoscriptBlock = false
        }
        else if (!inNoscriptBlock) {
            documentHandler.endElement(qName, augs)
        }
    }
    
    @Override
    void characters(XMLString xmlString, Augmentations augs) {
        if (!inNoscriptBlock) {
            documentHandler.characters(xmlString, augs)
        }
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) {
        if (!inNoscriptBlock) {
            documentHandler.emptyElement(element, attributes, augs)
        }
    }

}
