package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation

/**
 * Rewrites <tt>src</tt>, <tt>href</tt> and <tt>action</tt> attributes.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class URIRewritingContentHandler extends RewritingContentHandler {

    @Override    
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        atts.length.times { int i ->
            String attributeValue = trimToEmpty(atts.getValue(i))
            
            // Use local name
            if (attributeNameShouldBeRewritten(atts.getLocalName(i)) && attributeValueNeedsRewriting(attributeValue)) {
                atts.setValue(i, rewrite(baseURI, requestURI, attributeValue, rewriteConfig))
            }
        }
        documentHandler.startElement(qName, atts, augs)
    }
    
    @CompileStatic
    private boolean attributeNameShouldBeRewritten(String attributeName) {
        switch (attributeName) {
            case 'value':
            case 'object':
            case 'archive':
            case 'icon':
            case 'code':
            case 'codebase':
            case 'movie':
            case 'data':
            case 'poster':
            case 'formaction':
            case 'longdesc':
            case 'lowsrc':
            case 'dynsrc':
            case 'manifest':
            case 'implementation':
            case 'background':
            case 'href':
            case 'src':
            case 'action':
            case 'ping': return true
        }
        false
    }

}
