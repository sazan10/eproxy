package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*
import groovy.util.logging.Slf4j

import org.ccil.cowan.tagsoup.AttributesImpl
import org.springframework.web.util.UriComponentsBuilder
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Rewrites <tt>src</tt> and <tt>href</tt> attributes.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Slf4j
class URIRewritingContentHandler extends URIAwareContentHandler {

    RewriteConfig rewriteConfig

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        atts?.length?.times { int i ->
            String attributeName = name(atts.getLocalName(i), atts.getQName(i)), attributeValue = trimToEmpty(atts.getValue(i))
            if ((equalsIgnoreCase(attributeName, 'href') || equalsIgnoreCase(attributeName, 'src')) &&
                (attributeValue.startsWith('/') || startsWithIgnoreCase(attributeValue, 'http:') || startsWithIgnoreCase(attributeValue, 'https:'))) {
                
                ((AttributesImpl) atts).setValue(i, rewriteURI(baseURI, attributeValue.toURI()) as String)
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }
    
    // TODO: Copy & paste!
    URI rewriteURI(URI baseURI, URI target) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI).pathSegment(target.scheme, target.authority)
        if (target.rawPath) {
            builder.path(target.rawPath)
        }
        builder.query(target.rawQuery).fragment(target.rawFragment).build().toUri()
    }

}
