package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.entities.RewriteConfig

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@InheritConstructors
@Slf4j
class URIRewritingContentHandler extends URIAwareContentHandler {

    RewriteConfig rewriteConfig

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        atts?.length?.times { int i ->
            String attributeName = name(atts.getLocalName(i), atts.getQName(i))
            if (equalsIgnoreCase(attributeName, 'href') || equalsIgnoreCase(attributeName, 'src')) {
                //log.info('{} {}={}', name(localName, qName), attributeName, atts.getValue(i))
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }

}
