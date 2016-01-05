package com.eaio.eproxy.rewriting.css

import static org.apache.commons.lang3.StringUtils.*
import groovy.util.logging.Slf4j

import org.w3c.css.sac.InputSource
import org.w3c.css.sac.LexicalUnit
import org.w3c.dom.css.*
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.URLManipulation
import com.eaio.eproxy.rewriting.html.URIAwareContentHandler
import com.steadystate.css.dom.*
import com.steadystate.css.parser.CSSOMParser
import com.steadystate.css.parser.LexicalUnitImpl
import com.steadystate.css.parser.SACParserCSS3

/**
 * Rewrites CSS using <a href="http://cssparser.sourceforge.net/">CSS Parser</a>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
@Slf4j
class CSSRewritingContentHandler extends URIAwareContentHandler {

    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'style')) {
            stack.push('style')
        }
        else {
            String styleAttribute = atts?.getValue('style')
            if (styleAttribute && styleAttribute.length() > 5I) {
                String rewrittenCSS = rewriteStyleAttribute(styleAttribute)
                setAttributeValue(atts, atts.getIndex('style'), rewrittenCSS)
            }
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
        if (tag == 'style' && length > 5I) {
            DirectStrBuilder builder = new DirectStrBuilder(length)
            Reader charArrayReader = new CharArrayReader(ch, start, length)
            rewriteCSS(charArrayReader, builder.asWriter())
            delegate.characters(builder.buffer, 0I, builder.length())

            //println "\nrewrote\n${new String(ch, start, length)}\nto\n${new String(builder.buffer, 0I, builder.length())}"
        }
        else {
            delegate.characters(ch, start, length)
        }
    }

    CSSOMParser newCSSOMParser() {
        new CSSOMParser(new SACParserCSS3())
    }

    String rewriteStyleAttribute(String attribute) {
        InputSource source = new InputSource(characterStream: new StringReader(attribute))
        CSSOMParser parser = newCSSOMParser()
        CSSStyleDeclarationImpl declaration = (CSSStyleDeclarationImpl) parser.parseStyleDeclaration(source)
        rewriteCSSStyleDeclaration(declaration)
        declaration as String
    }

    void rewriteCSS(Reader reader, Writer writer) {
        InputSource source = new InputSource(characterStream: reader)
        CSSOMParser parser = newCSSOMParser()
        CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null)
        sheet.cssRules.length.times { int i ->
            CSSRule rule = sheet.cssRules.item(i)
            rewriteCSSRule(rule)
        }
        writer.write(sheet as String) // TODO
    }
    
    void rewriteCSSStyleDeclaration(CSSStyleDeclarationImpl declaration) {
        declaration.properties*.value.each { CSSValue value ->
            rewriteCSSValueImpl((CSSValueImpl) value)
        }
    }

    void rewriteCSSRule(CSSRule rule) {
        if (rule instanceof CSSStyleRuleImpl || rule instanceof CSSFontFaceRuleImpl) {
            rewriteCSSStyleDeclaration(rule.style)
        }
        else if (rule instanceof CSSImportRuleImpl) {
            rule.href = rewrite(baseURI, requestURI, rule.href, rewriteConfig)
        }
        else if (rule instanceof CSSUnknownRuleImpl) {
            if (containsIgnoreCase(rule.text, 'url')) {
                log.warn("unknown CSS rule in ${requestURI}: ${rule.text}")
            }
            // TODO: Remove
        }
        else if (rule instanceof CSSMediaRuleImpl) {
            rule.cssRules.length.times { int j ->
                rewriteCSSRule(rule.cssRules.item(j))
            }
        }
        else if (rule instanceof CSSCharsetRuleImpl) {}
        else {
            println rule.getClass().name
        }
    }

    void rewriteCSSValueImpl(CSSValueImpl value) {
        if (value.length > 0I) {
            value.length.times { int k ->
                CSSValueImpl item = (CSSValueImpl) value.item(k)
                rewriteCSSValueImpl(item)
            }
        }
        else if (containsURILexicalUnit(value) && !isDataURI((LexicalUnitImpl) value.value)) {
            value.value.stringValue = rewrite(baseURI, requestURI, value.value.stringValue, rewriteConfig)
        }
    }

    boolean containsURILexicalUnit(CSSValueImpl value) {
        value.value instanceof LexicalUnit && value.value.lexicalUnitType == LexicalUnit.SAC_URI
    }

    boolean isDataURI(LexicalUnitImpl lexicalUnit) {
        lexicalUnit.stringValue?.trim()?.startsWith('data:') // TODO Case insensitive
    }

}
