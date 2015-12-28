package com.eaio.eproxy.rewriting.css

import groovy.util.logging.Slf4j

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
@Slf4j
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
            DirectStrBuilder builder = new DirectStrBuilder(length)
            rewriteCSS(new CharArrayReader(ch, start, length), builder.asWriter())
            delegate.characters(builder.buffer, 0I, builder.length())
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

    void rewriteCSS(Reader reader, Writer writer) {
        CascadingStyleSheet css = CSSReader.readFromReader(new HasReaderImpl(reader: reader), newCSSReaderSettings())
        if (css == null) {
            // TODO...
            log.warn('couldn\'t parse {}', requestURI)
            IOUtils.copyLarge(reader, writer)
        }
        else {
            CSSVisitor.visitCSSUrl(css, this)
            newCSSWriter().writeCSS(css, writer)
        }
    }

    @Override
    void begin() {
    }

    @Override
    void onImport(CSSImportRule importRule) {
        if (!importRule.location.dataURL) {
            importRule.location.URI = rewrite(baseURI, resolve(requestURI, importRule.location.URI), rewriteConfig)
        }
    }

    @Override
    void onUrlDeclaration(ICSSTopLevelRule topLevelRule, CSSDeclaration declaration, CSSExpressionMemberTermURI uriTerm) {
        if (!uriTerm.URI.dataURL) {
            uriTerm.URI.URI = rewrite(baseURI, resolve(requestURI, uriTerm.URI.URI), rewriteConfig)
        }
    }

    @Override
    void end() {
    }

}
