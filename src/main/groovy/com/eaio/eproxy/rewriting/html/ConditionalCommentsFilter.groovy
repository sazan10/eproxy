package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.xerces.util.XMLAttributesImpl
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString
import org.apache.xerces.xni.XNIException
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Transforms HTML as follows:
 * <ul>
 * <li>Removes conditional comments because they're a security nightmare.
 * </ul>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
class ConditionalCommentsFilter extends BaseFilter {
    
    /**
     * @see org.cyberneko.html.filters.DefaultFilter#comment(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    @Override
    void comment(XMLString text, Augmentations augs) throws XNIException {
        String comment = text?.toString()?.toLowerCase()
        if (!comment?.contains('[if') && !comment?.contains('[endif]')) {
            super.comment(text, augs)
        }
    }
    
}
