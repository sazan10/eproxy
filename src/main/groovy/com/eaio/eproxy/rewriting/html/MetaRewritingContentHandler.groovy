package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.apache.http.message.ParserCursor
import org.apache.http.util.CharArrayBuffer
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation
import com.eaio.stringsearch.BNDMCI

/**
 * Rewrites <tt>meta refresh</tt>. Should be placed after {@link RemoveNoScriptElementsContentHandler}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class MetaRewritingContentHandler extends RewritingContentHandler {

    @Lazy
    private transient BNDMCI bndmci = new BNDMCI()

    @Lazy
    private transient Object patternRefresh = bndmci.processString('refresh'),
        patternURL = bndmci.processString('url')

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'meta')) {
            String httpEquiv = atts.getValue('http-equiv')
            if (httpEquiv) {
                String content = atts.getValue('content')
                if (content && bndmci.searchString(httpEquiv, 'refresh', patternRefresh) >= 0I && bndmci.searchString(content, 'url', patternURL) >= 0I) {
                    int i = atts.getIndex('content')
                    CharArrayBuffer buf = new CharArrayBuffer(content.length())
                    buf.append(content)
                    ParserCursor cursor = new ParserCursor(0I, content.length())
                    HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(buf, cursor)
                    String url = elements[0I]?.getParameterByName('URL')?.value
                    if (url) {
                        String rewrittenURL = rewrite(baseURI, requestURI, url.replaceFirst('^["\']', '').replaceFirst('["\']$', ''), rewriteConfig)
                        setAttributeValue(atts, i, replaceOnce(content, url, rewrittenURL))
                    }
                }
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }

}
