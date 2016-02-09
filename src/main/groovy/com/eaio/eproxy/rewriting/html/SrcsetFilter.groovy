package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes

import com.eaio.eproxy.rewriting.URLManipulation

/**
 * Rewrites <tt>&lt;img srcset&gt;</tt> and <tt>&lt;source srcset&gt;</tt>, which is slightly different due to being a comma-separated list.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
class SrcsetFilter extends RewritingFilter implements URLManipulation {

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.startElement(qName, atts, augs)
    }

    @Override
    public void emptyElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.emptyElement(qName, atts, augs)
    }

    private void rewriteElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'img') || nameIs(qName, 'source')) {
            String attributeValue = atts.getValue('srcset')
            if (attributeValue) {
                int i = atts.getIndex('srcset')
                if (attributeValue.contains(',')) {
                    List<String> parts = attributeValue.tokenize(',')
                    parts.size().times { int index ->
                        if (attributeValueNeedsRewriting(parts[index])) {
                            String imageURI = substringBefore(parts[index], ' ') ?: parts[index]
                            parts[index] = replaceOnce(parts[index], imageURI, rewrite(baseURI, requestURI, imageURI, rewriteConfig))
                        }
                    }
                    atts.setValue(i, parts.join(','))
                }
                else if (attributeValueNeedsRewriting(attributeValue)) {
                    String imageURI = substringBefore(attributeValue, ' ') ?: attributeValue
                    atts.setValue(i, replaceOnce(attributeValue, imageURI, rewrite(baseURI, requestURI, imageURI, rewriteConfig)))
                }
            }
        }
    }
    
}
