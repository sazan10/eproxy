package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.apache.http.message.ParserCursor
import org.apache.http.util.CharArrayBuffer
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation

/**
 * Rewrites <tt>meta refresh</tt>. Should be placed after {@link RemoveNoScriptElementsContentHandler}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class MetaRewritingContentHandler extends URIAwareContentHandler {
    
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'meta') && containsIgnoreCase(atts.getValue('http-equiv'), 'refresh')) {
            String content = atts.getValue('content')
            if (containsIgnoreCase(content, 'url')) {
                CharArrayBuffer buf = new CharArrayBuffer(content.length())
                buf.append(content)
                ParserCursor cursor = new ParserCursor(0I, content.length())
                HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(buf, cursor)
                String url = elements[0I]?.getParameterByName('URL')?.value,
                    rewrittenURL = rewrite(baseURI, resolve(requestURI, url?.replaceFirst('^["\']', '').replaceFirst('["\']$', ''),), rewriteConfig)
                setAttributeValue(atts, atts.getIndex('content'), replaceOnce(content, url, rewrittenURL))
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }
    
}
