package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.xerces.xni.*
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import com.eaio.eproxy.rewriting.URIManipulation

/**
 * Rewrites all attributes except those already covered by other {@link BaseFilter filters}.
 * Also ignores <tt>xmlns</tt> and <tt>xmlns</tt>-prefixed attributes.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
class URIRewritingFilter extends RewritingFilter implements URIManipulation {

    @Override
    void processingInstruction(String target, XMLString data,
                    Augmentations augs) {
        XMLString xmlString = data
        String s = data as String
        if (s?.toLowerCase()?.contains("href=")) {
            s = s.replaceAll(~/href=["']([^#][^"']+)["']/, { List<String> matches ->
                String out = matches[0I]
                String uri = matches[1I]
                if (attributeValueNeedsRewriting(uri)) {
                    String rewritten = encodeTargetURI(baseURI, requestURI, uri, rewriteConfig)
                    out = replace(matches[0I], uri, rewritten)
                }
                out
            })
            xmlString = new XMLString(s.toCharArray(), 0I, s.length()) // TODO performance
        }
        super.processingInstruction(target, xmlString, augs);
    }

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
        for (int i = 0; i < atts.length; ++i) {
            String attributeValue = trimToEmpty(atts.getValue(i))
            
            // Use local name
            if (!equalsIgnoreCase(atts.getLocalName(i), 'style') && !startsWithIgnoreCase(atts.getLocalName(i), 'on') &&
                !equalsIgnoreCase(atts.getLocalName(i), 'srcset') && !equalsIgnoreCase(atts.getLocalName(i), 'http-equiv') &&
                !equalsIgnoreCase(atts.getLocalName(i), 'content') &&
                !equalsIgnoreCase(atts.getPrefix(i), 'xmlns') && !equalsIgnoreCase(atts.getQName(i), 'xmlns') && // Don't rewrite xmlns namespaced attributes.
                !equalsIgnoreCase(atts.getLocalName(i), 'srcdoc') && // See RecursiveInlineHTMLRewritingFilter
                attributeValueNeedsRewriting(attributeValue)) {
                atts.setValue(i, encodeTargetURI(baseURI, requestURI, attributeValue, rewriteConfig))
            }
        }
    }

}
