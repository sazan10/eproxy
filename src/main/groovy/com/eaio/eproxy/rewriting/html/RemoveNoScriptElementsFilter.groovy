package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Transforms HTML as follows:
 * <ul>
 * <li>Any <tt>&lt;noscript&gt;</tt> elements and their child nodes are removed.
 * </ul>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
    void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) {
        if (!inNoscriptElement) {
            super.emptyElement(element, attributes, augs)
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

}
