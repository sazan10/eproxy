package com.eaio.eproxy.rewriting.svg

import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XNIException

import com.eaio.eproxy.rewriting.html.BaseFilter

/**
 * For SVG: Turns empty elements into start and end elements to prevent
 * browsers from constructing a different tree. 
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
class SVGFilter extends BaseFilter {
    
    private boolean inSVGElement

    @Override
    void startElement(QName element, XMLAttributes attributes,
                    Augmentations augs) throws XNIException {
        if (nameIs(element, 'svg')) {
            inSVGElement = true
        }
        super.startElement(element, attributes, augs);
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes,
                    Augmentations augs) throws XNIException {
        if (inSVGElement) {
            super.startElement(element, attributes, augs)
            super.endElement(element, augs)
        }
        else {
            super.emptyElement(element, attributes, augs)
        }
    }

    @Override
    void endElement(QName element, Augmentations augs)
                    throws XNIException {
        if (nameIs(element, 'svg')) {
            inSVGElement = false
        }
        super.endElement(element, augs)
    }    
    
}
