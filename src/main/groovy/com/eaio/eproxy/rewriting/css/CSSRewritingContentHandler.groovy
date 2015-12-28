package com.eaio.eproxy.rewriting.css

import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.rewriting.URLManipulation
import com.eaio.eproxy.rewriting.html.URIAwareContentHandler
import com.helger.css.ECSSVersion
import com.helger.css.decl.*
import com.helger.css.decl.visit.CSSVisitor
import com.helger.css.decl.visit.ICSSUrlVisitor
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
class CSSRewritingContentHandler extends URIAwareContentHandler implements ICSSUrlVisitor {

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
    
    CSSWriter newCSSWriter() {
        new CSSWriter(ECSSVersion.CSS30, true)
    }

    String rewriteStyleAttribute(String attribute) {
        CSSDeclarationList declarations = CSSReaderDeclarationList.readFromString(attribute, ECSSVersion.CSS30)
        if (declarations == null) {
            // TODO...
        }
        CSSVisitor.visitAllDeclarationUrls(declarations, this)
        newCSSWriter().getCSSAsString(declarations)
    }

    String rewriteCSS(Reader reader) {
        CascadingStyleSheet css = CSSReader.readFromReader(new HasReaderImpl(reader: reader), newCSSReaderSettings())
        if (css == null) {
            // TODO...
        }
        CSSVisitor.visitCSSUrl(css, this)
        newCSSWriter().getCSSAsString(css)
    }

    @Override
    void begin() {
    }

    @Override
    void onImport(CSSImportRule importRule) {
        importRule.location.URI = rewrite(baseURI, resolve(requestURI, importRule.location.URI), rewriteConfig)
    }

    @Override
    void onUrlDeclaration(ICSSTopLevelRule topLevelRule,
            CSSDeclaration declaration, CSSExpressionMemberTermURI uriTerm) {
        uriTerm.URI.URI = rewrite(baseURI, resolve(requestURI, uriTerm.URI.URI), rewriteConfig)
    }

    @Override
    void end() {
    }

}
