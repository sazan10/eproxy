package com.eaio.eproxy.rewriting.html

import groovy.util.logging.Slf4j

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XNIException

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Slf4j
class DebuggingFilter extends BaseFilter {

    @Override
    void startElement(QName element, XMLAttributes attributes,
                    Augmentations augs) throws XNIException {
        log.info('startElement {}', element)
        super.startElement(element, attributes, augs)
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes,
                    Augmentations augs) throws XNIException {
        log.info('emptyElement {}', element)
        super.emptyElement(element, attributes, augs)
    }

}
