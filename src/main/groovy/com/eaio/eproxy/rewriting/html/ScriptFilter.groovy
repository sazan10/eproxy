package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import org.apache.xerces.util.XMLAttributesImpl
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Adds a script pointing to {@link com.eaio.eproxy.api.ScriptRedirect} to the page (if enabled).
 * <p>
 * Needs to be placed after {@link RemoveActiveContentFilter} obviously.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Component
@CompileStatic
class ScriptFilter extends BaseFilter {
    
    @Value('${script.redirect}')
    String scriptRedirect
    
    @Lazy
    private QName scriptElement = new QName(null, 'script', 'script', null),
        asyncAttribute = new QName(null, 'async', 'async', null),
        srcAttribute = new QName(null, 'src', 'src', null)
    
    @Override
    void endElement(QName element, Augmentations augs) {
        if (scriptRedirect && nameIs(element, 'body')) {
            writeProxyJavaScript()
        }
        super.endElement(element, augs)
    }
    
    private void writeJavaScript(XMLString xmlString) {
        super.startElement(scriptElement, null, null)
        super.characters(xmlString, null)
        super.endElement(scriptElement, null)
    }
    
    private writeProxyJavaScript() {
        XMLAttributes atts = new XMLAttributesImpl(2I)
        atts.addAttribute(asyncAttribute, null, 'async')
        atts.addAttribute(srcAttribute, null, (((ServletRequestAttributes) RequestContextHolder.requestAttributes)?.request?.contextPath ?: '') +  '/script')
        super.startElement(scriptElement, atts, null)
        super.endElement(scriptElement, null)
    }

}
