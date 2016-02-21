package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.apache.http.message.ParserCursor
import org.apache.http.util.CharArrayBuffer
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes

import com.eaio.eproxy.rewriting.URIManipulation

/**
 * Rewrites <tt>meta refresh</tt>. Should be placed after {@link RemoveNoScriptElementsFilter}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Slf4j
class MetaRewritingFilter extends RewritingFilter implements URIManipulation {

    @Lazy
    private transient Object patternRefresh = bndmci.processString('refresh'),
        patternURL = bndmci.processString('url')

    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'meta')) {
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
                        String rewrittenURL = encodeTargetURI(baseURI, requestURI, url.replaceFirst('^["\']', '').replaceFirst('["\']$', ''), rewriteConfig)
                        atts.setValue(i, replaceOnce(content, url, rewrittenURL))
                    }
                }
            }
        }
        super.startElement(qName, atts, augs)
    }

}
