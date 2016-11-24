package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import com.eaio.stringsearch.BNDMCI

/**
 * Replaces javascript: URIs with inactive values.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
class RemoveJavaScriptURIFilter extends BaseFilter {

    @Lazy
    private BNDMCI bndmci = new BNDMCI()

    @Lazy
    private def patternJavaScript = bndmci.processString('javascript:')

    @Override
    void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) {
        rewriteElement(element, attributes, augs)
        super.startElement(element, attributes, augs)
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) {
        rewriteElement(element, attributes, augs)
        super.emptyElement(element, attributes, augs)
    }

    private void rewriteElement(QName qName, XMLAttributes atts, Augmentations augs) {
        int srcIndex = atts.getIndex('src')
        if (srcIndex >= 0I) {
            int javascriptIndex = bndmci.searchString(atts.getValue(srcIndex) ?: '', 'javascript:', patternJavaScript)
            if (javascriptIndex >= 0I) {
                atts.setValue(srcIndex, 'javascript:""')
            }
        }
    }

}
