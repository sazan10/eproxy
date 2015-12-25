package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*

import org.apache.xerces.parsers.AbstractSAXParser.AttributesProxy
import org.ccil.cowan.tagsoup.AttributesImpl
import org.springframework.web.util.UriComponentsBuilder
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Rewrites <tt>src</tt>, <tt>href</tt> and <tt>action</tt> attributes.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URIRewritingContentHandler extends URIAwareContentHandler {

    RewriteConfig rewriteConfig

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        atts?.length?.times { int i ->
            String attributeName = name(atts.getLocalName(i), atts.getQName(i)), attributeValue = trimToNull(atts.getValue(i))
            if ((equalsIgnoreCase(attributeName, 'href') || equalsIgnoreCase(attributeName, 'src') || equalsIgnoreCase(attributeName, 'action')) &&
                (attributeValue.startsWith('/') || startsWithIgnoreCase(attributeValue, 'http:') || startsWithIgnoreCase(attributeValue, 'https:'))) {
                
                def resolvedAttributeValue = resolve(requestURI, attributeValue)
                
                //((AttributesImpl) atts).setValue(i, rewrite(baseURI, resolvedAttributeValue) as String) // TagSoup
                ((AttributesProxy) atts).@fAttributes.setValue(i, rewrite(baseURI, resolvedAttributeValue) as String) // NekoHTML
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }
    
    // TODO: Copy & paste!
    URI rewrite(URI baseURI, URI target) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI).pathSegment((rewriteConfig ? 'ah-' : '') + target.scheme, target.authority)
        if (target.rawPath) {
            builder.path(target.rawPath)
        }
        builder.query(target.rawQuery).fragment(target.rawFragment).build().toUri()
    }
    
    /**
     * Alternative rewriting method for URIs that {@link URI} doesn't want to parse.
     */
    String rewrite(URI baseURI, URL target) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI).pathSegment((rewriteConfig ? 'ah-' : '') + target.protocol, target.authority)
        if (target.path) {
            builder.path(target.path)
        }
        builder.query(target.query).fragment(target.ref).build().toUriString()
    }
    
    /**
     * @return either a {@link URI} or a {@link URL}
     */
    def resolve(URI requestURI, String attributeValue) {
        try {
            requestURI.resolve(attributeValue)
        }
        catch (IllegalArgumentException ex) {
            new URL(new URL(requestURI.scheme, requestURI.host, requestURI.port, requestURI.rawPath), attributeValue)
        }
    }

}
