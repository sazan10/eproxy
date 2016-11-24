package com.eaio.eproxy.rewriting.svg

import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XNIException
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import com.eaio.eproxy.rewriting.html.BaseFilter

/**
 * For SVG: Turns empty elements into start and end elements to prevent
 * browsers from constructing a different tree. 
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SVGFilter extends BaseFilter {
    
    private boolean inSVGElement

    @Override
    void startElement(QName qName, XMLAttributes attributes,
                    Augmentations augs) throws XNIException {
        if (nameIs(qName, 'svg')) {
            inSVGElement = true
        }
        super.startElement(qName, attributes, augs)
    }

    @Override
    void emptyElement(QName qName, XMLAttributes attributes,
                    Augmentations augs) throws XNIException {
        if (inSVGElement) {
            super.startElement(qName, attributes, augs)
            super.endElement(qName, augs)
        }
        else {
            super.emptyElement(qName, attributes, augs)
        }
    }

    @Override
    void endElement(QName qName, Augmentations augs)
                    throws XNIException {
        if (nameIs(qName, 'svg')) {
            inSVGElement = false
        }
        super.endElement(qName, augs)
    }    
    
}
