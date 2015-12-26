package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.apache.http.message.ParserCursor
import org.apache.http.util.CharArrayBuffer
import org.xml.sax.Attributes
import org.xml.sax.SAXException

/**
 * Rewrites <tt>meta refresh</tt>. Should be placed after {@link RemoveNoScriptElementsContentHandler}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class MetaRewritingContentHandler extends URIAwareContentHandler {
    
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'meta') && containsIgnoreCase(atts.getValue('http-equiv'), 'refresh') && containsIgnoreCase(atts.getValue('content'), 'url')) {
            String url = getURL(atts.getValue('content'))
            if (url) {
                // TODO: broken
                setAttributeValue(atts, atts.getIndex('content'), rewrite(baseURI, resolve(requestURI, url), rewriteConfig))
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }
    
    String getURL(String refreshValue) {
        CharArrayBuffer buf = new CharArrayBuffer(refreshValue.length())
        buf.append(refreshValue)
        ParserCursor cursor = new ParserCursor(0I, refreshValue.length())
        HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(buf, cursor)
        elements?.getAt(0I)?.getParameterByName('URL')?.value
    }

}
