package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.charset.Charset

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import com.eaio.eproxy.rewriting.Rewriting

/**
 * Transforms HTML as follows:
 * <ul>
 * <li>Any srcdoc attributes are run through {@link Rewriting}
 * </ul>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
class SrcdocFilter extends RewritingFilter implements BeanFactoryAware {

    @Lazy
    private Charset defaultCharset = Charset.forName('UTF-8')

    // For some reason, reusing the Rewriting instance doesn't work so this class needs its own Rewriting instance.
    @Lazy
    private Rewriting rewriting = new Rewriting(beanFactory: beanFactory)

    BeanFactory beanFactory

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.startElement(qName, atts, augs)
    }

    @Override
    void emptyElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.emptyElement(qName, atts, augs)
    }     

    private void rewriteElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'iframe')) {
            int srcDocIndex = atts.getIndex('srcdoc')
            if (srcDocIndex >= 0I) {
                String srcdocValue = atts.getValue(srcDocIndex)
                ByteArrayOutputStream bOut = new ByteArrayOutputStream(srcdocValue.length())
                rewriting.rewriteHTMLFragment(new ByteArrayInputStream(srcdocValue.getBytes(defaultCharset)), bOut, defaultCharset, baseURI, requestURI, rewriteConfig)
                String rewrittenSrcdocValue = bOut.toString(defaultCharset.name())
                log.debug('rewrote srcdoc attribute from\n{}\nto\n{}', atts.getValue(srcDocIndex), rewrittenSrcdocValue)
                atts.setValue(srcDocIndex, rewrittenSrcdocValue)
            }
        }
    }

}
