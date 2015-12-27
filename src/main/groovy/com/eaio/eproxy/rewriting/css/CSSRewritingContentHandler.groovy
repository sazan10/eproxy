package com.eaio.eproxy.rewriting.css

import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation
import com.eaio.eproxy.rewriting.html.URIAwareContentHandler
import com.helger.css.ECSSVersion
import com.helger.css.decl.CSSDeclarationList
import com.helger.css.decl.CSSExpressionMemberTermURI
import com.helger.css.decl.CascadingStyleSheet
import com.helger.css.decl.IHasCSSDeclarations
import com.helger.css.reader.CSSReader
import com.helger.css.reader.CSSReaderDeclarationList
import com.helger.css.reader.CSSReaderSettings
import com.helger.css.writer.CSSWriter

/**
 * Rewrites URLs in embedded CSS and inline style sheets. Very much a WIP.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
class CSSRewritingContentHandler extends URIAwareContentHandler {

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'style')) {
            stack.push('style')
        }
        else if (atts.getValue('style')) {
            String rewrittenCSS = rewriteStyleAttribute(atts.getValue('style'))
            setAttributeValue(atts, atts.getIndex('style'), rewrittenCSS)
        }
        delegate.startElement(uri, localName, qName, atts)
    }

    void endElement(String uri, String localName, String qName) throws SAXException {
        if (nameIs(localName, qName, 'style')) {
            try {
                stack.pop()
            }
            catch (EmptyStackException ex) {}
        }
        delegate.endElement(uri, localName, qName)
    }

    /**
     * Rewrites <tt>&lt;style&gt;</tt> contents.
     */
    void characters(char[] ch, int start, int length) throws SAXException {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag == 'style') {
            String rewrittenCSS = rewriteCSS(new CharArrayReader(ch, start, length))
            delegate.characters(rewrittenCSS.toCharArray(), 0I, rewrittenCSS.length()) // TODO FFS
        }
        else {
            delegate.characters(ch, start, length)
        }
    }

    CSSReaderSettings newCSSReaderSettings() {
        new CSSReaderSettings(CSSVersion: ECSSVersion.CSS30)
    }

    String rewriteStyleAttribute(String attribute) {
        CSSDeclarationList declarations = CSSReaderDeclarationList.readFromString(attribute, ECSSVersion.CSS30)
        if (declarations == null) {
            //...
        }
        // TODO
        declarations.allDeclarations.each {
            it.expression.each {
                it.allMembers.findAll { it instanceof CSSExpressionMemberTermURI }.each {
                    it.URI.URI = rewrite(baseURI, resolve(requestURI, it.URI.URI), rewriteConfig)
                }
            }
        }
        new CSSWriter(ECSSVersion.CSS30, true).getCSSAsString(declarations)
    }

    String rewriteCSS(Reader reader) {
        CascadingStyleSheet css = CSSReader.readFromReader(new HasReaderImpl(reader: reader), newCSSReaderSettings())
        [ css.allRules, css.allImportRules, css.allNamespaceRules ].flatten().each {
            if (it instanceof IHasCSSDeclarations) {
                it.allDeclarations.each {
                    it.expression.each {
                        it.allMembers.findAll { it instanceof CSSExpressionMemberTermURI }.each {
                            it.URI.URI = rewrite(baseURI, resolve(requestURI, it.URI.URI), rewriteConfig)
                        }
                    }
                }
            }
        }
        new CSSWriter(ECSSVersion.CSS30, true).getCSSAsString(css)
    }

}
