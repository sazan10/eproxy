package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes

import com.eaio.eproxy.rewriting.URIManipulation

/**
 * Rewrites all attributes except those already covered by other {@link BaseFilter filters}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Slf4j
class URIRewritingFilter extends RewritingFilter implements URIManipulation {

    @Override    
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.startElement(qName, atts, augs)
    }
    
    @Override
    void emptyElement(QName element, XMLAttributes atts, Augmentations augs) {
        rewriteElement(element, atts, augs)
        super.emptyElement(element, atts, augs)
    }
    
    private void rewriteElement(QName qName, XMLAttributes atts, Augmentations augs) {
        atts.length.times { int i ->
            String attributeValue = atts.getValue(i)
            
            // Use local name
            if (!equalsIgnoreCase(atts.getLocalName(i), 'style') && !startsWithIgnoreCase(atts.getLocalName(i), 'on') &&
                !equalsIgnoreCase(atts.getLocalName(i), 'srcset') && !equalsIgnoreCase(atts.getLocalName(i), 'http-equiv') &&
                !equalsIgnoreCase(atts.getLocalName(i), 'content') &&
                attributeValueNeedsRewriting(attributeValue)) {
                atts.setValue(i, encodeTargetURI(baseURI, requestURI, attributeValue, rewriteConfig))
            }
        }
    }

}
