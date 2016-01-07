package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*

import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation

/**
 * Rewrites <tt>&lt;img srcset&gt;</tt>, which is slightly different due to being a comma-separated list.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class ImgSrcsetRewritingContentHandler extends RewritingContentHandler {

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'img')) {
            String attributeValue = atts.getValue('srcset')
            if (attributeValue) {
                int i = atts.getIndex('srcset')
                if (attributeValue.contains(',')) {
                    List<String> parts = attributeValue.tokenize(',')
                    parts.size().times { int index ->
                        if (attributeValueNeedsRewriting(parts[index])) {
                            String imageURI = substringBefore(parts[index], ' ')
                            parts[index] = replaceOnce(parts[index], imageURI, rewrite(baseURI, requestURI, imageURI, rewriteConfig))
                        }
                    }
                    setAttributeValue(atts, i, parts.join(','))
                }
                else if (attributeValueNeedsRewriting(attributeValue)) {
                    String imageURI = substringBefore(attributeValue, ' ')
                    setAttributeValue(atts, i, replaceOnce(attributeValue, imageURI, rewrite(baseURI, requestURI, imageURI, rewriteConfig)))
                }
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }

}
