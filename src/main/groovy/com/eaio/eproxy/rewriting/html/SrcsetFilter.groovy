package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes

import com.eaio.eproxy.rewriting.URIManipulation

/**
 * Rewrites <tt>&lt;img srcset&gt;</tt> and <tt>&lt;source srcset&gt;</tt>, which is rewritten differently due to being a comma-separated list.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Slf4j
class SrcsetFilter extends RewritingFilter implements URIManipulation {

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
        if (nameIs(qName, 'img') || nameIs(qName, 'source')) {
            String attributeValue = atts.getValue('srcset')
            if (attributeValue) {
                int i = atts.getIndex('srcset')
                if (attributeValue.contains(',')) {
                    List<String> parts = attributeValue.tokenize(',').collect { it.trim() }
                    parts.size().times { int index ->
                        if (attributeValueNeedsRewriting(parts[index])) {
                            String imageURI = getImageURI(parts[index])
                            parts[index] = replace(parts[index], imageURI, encodeTargetURI(baseURI, requestURI, imageURI, rewriteConfig))
                        }
                    }
                    atts.setValue(i, parts.join(', '))
                }
                else if (attributeValueNeedsRewriting(attributeValue)) {
                    String imageURI = getImageURI(attributeValue)
                    atts.setValue(i, replace(attributeValue, imageURI, encodeTargetURI(baseURI, requestURI, imageURI, rewriteConfig)))
                }
            }
        }
    }
    
    private String getImageURI(String src) {
        substringBefore(src, ' ') ?: src
    }
    
}
