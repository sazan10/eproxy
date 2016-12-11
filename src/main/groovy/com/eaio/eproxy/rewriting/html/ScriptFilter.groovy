package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import org.apache.xerces.util.XMLAttributesImpl
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
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
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ScriptFilter extends BaseFilter {
    
    @Value('${script.redirect}')
    String scriptRedirect
    
    private boolean scriptWritten
    
    @Lazy
    private QName scriptElement = new QName(null, 'script', 'script', null),
        asyncAttribute = new QName(null, 'async', 'async', null),
        srcAttribute = new QName(null, 'src', 'src', null)
    
    @Override
    void endElement(QName element, Augmentations augs) {
        if (scriptRedirect && !scriptWritten && nameIs(element, 'body')) {
            writeScriptElement()
            scriptWritten = true
        }
        super.endElement(element, augs)
    }
    
    private writeScriptElement() {
        XMLAttributes atts = new XMLAttributesImpl(2I)
        atts.addAttribute(asyncAttribute, null, 'async')
        atts.addAttribute(srcAttribute, null, (((ServletRequestAttributes) RequestContextHolder.requestAttributes)?.request?.contextPath ?: '') +  '/script')
        super.startElement(scriptElement, atts, null)
        super.endElement(scriptElement, null)
    }

}
