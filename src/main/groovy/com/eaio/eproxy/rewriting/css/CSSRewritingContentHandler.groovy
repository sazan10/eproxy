package com.eaio.eproxy.rewriting.css

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.lang3.exception.ExceptionUtils
import org.w3c.css.sac.*
import org.w3c.dom.css.*
import org.xml.sax.Attributes
import org.xml.sax.SAXException

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.URLManipulation
import com.eaio.eproxy.rewriting.html.RewritingContentHandler
import com.steadystate.css.dom.*
import com.steadystate.css.parser.CSSOMParser
import com.steadystate.css.parser.SACParserCSS3

/**
 * Rewrites CSS using <a href="http://cssparser.sourceforge.net/">CSS Parser</a>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Mixin(URLManipulation)
@Slf4j
class CSSRewritingContentHandler extends RewritingContentHandler implements ErrorHandler {

    @CompileStatic
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (nameIs(localName, qName, 'style')) {
            stack.push('style')
        }
        else {
            String styleAttribute = atts.getValue('style')
            if (styleAttribute && styleAttribute.length() > 8I) {
                String rewrittenCSS = rewriteStyleAttribute(new InputSource(characterStream: new StringReader(styleAttribute)))
                setAttributeValue(atts, atts.getIndex('style'), rewrittenCSS)
                if (log.debugEnabled) {
                    log.debug('rewrote style attribute {} chars to {} chars{}', styleAttribute.length(), rewrittenCSS.length(), styleAttribute == rewrittenCSS ? ' (identical)' : '')
                }
            }
        }
        delegate.startElement(uri, localName, qName, atts)
    }

    @CompileStatic
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
    @CompileStatic
    void characters(char[] ch, int start, int length) throws SAXException {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag == 'style' && length > 11I) {
            DirectStrBuilder builder = new DirectStrBuilder(length)
            Reader charArrayReader = new CharArrayReader(ch, start, length)
            rewriteCSS(new InputSource(characterStream: charArrayReader), builder.asWriter())
            delegate.characters(builder.buffer, 0I, builder.length())
            if (log.debugEnabled) {
                log.debug('rewrote CSS {} chars to {} chars{}', length, builder.length(), new String(ch, start, length) == new String(builder.buffer, 0I, builder.length()) ? ' (identical)' : '')
            }
        }
        else {
            delegate.characters(ch, start, length)
        }
    }

    @CompileStatic
    CSSOMParser newCSSOMParser() {
        CSSOMParser out = new CSSOMParser(new SACParserCSS3())
        out.errorHandler = this
        out
    }

    @CompileStatic
    String rewriteStyleAttribute(InputSource source) {
        CSSOMParser parser = newCSSOMParser()
        CSSStyleDeclarationImpl declaration = (CSSStyleDeclarationImpl) parser.parseStyleDeclaration(source)
        rewriteCSSStyleDeclaration(declaration)
        declaration as String
    }

    @CompileStatic
    void rewriteCSS(InputSource inputSource, Writer writer) {
        CSSOMParser parser = newCSSOMParser()
        CSSStyleSheet sheet = parser.parseStyleSheet(inputSource, null, null)
        sheet.cssRules.length.times { int i ->
            CSSRule rule = sheet.cssRules.item(i)
            rewriteCSSRule(rule)
        }
        writer.write(sheet as String) // TODO
    }

    void rewriteCSSRule(CSSRule rule) {
        if (rule instanceof CSSStyleRuleImpl || rule instanceof CSSFontFaceRuleImpl || rule instanceof CSSPageRuleImpl) {
            rewriteCSSStyleDeclaration(rule.style)
        }
        else if (rule instanceof CSSImportRuleImpl) {
            rule.href = rewrite(baseURI, requestURI, rule.href, rewriteConfig)
        }
        else if (rule instanceof CSSUnknownRuleImpl) {
            log.warn('unknown CSS rule in {}: {}', requestURI, rule.text)
        }
        else if (rule instanceof CSSMediaRuleImpl) {
            rule.cssRules.length.times { int j ->
                rewriteCSSRule(rule.cssRules.item(j))
            }
        }
        else if (rule instanceof CSSCharsetRuleImpl) {}
        else {
            log.warn('unknown CSS rule type in {}: {}', requestURI, rule.getClass().name)
        }
    }
    
    @CompileStatic
    void rewriteCSSStyleDeclaration(CSSStyleDeclarationImpl declaration) {
        declaration.properties*.value.each { CSSValue value ->
            rewriteCSSValueImpl((CSSValueImpl) value)
        }
    }

    void rewriteCSSValueImpl(CSSValueImpl value) {
        if (value.length > 0I) {
            value.length.times { int k ->
                CSSValueImpl item = (CSSValueImpl) value.item(k)
                rewriteCSSValueImpl(item)
            }
        }
        else if (containsURILexicalUnit(value) && attributeValueNeedsRewriting(value.value.stringValue)) {
            value.value.stringValue = rewrite(baseURI, requestURI, value.value.stringValue, rewriteConfig)
        }
    }

    @CompileStatic
    boolean containsURILexicalUnit(CSSValueImpl value) {
        value.value instanceof LexicalUnit && ((LexicalUnit) value.value).lexicalUnitType == LexicalUnit.SAC_URI
    }

    @CompileStatic
    @Override
    void warning(CSSParseException exception) throws CSSException {
        log.info('while parsing {}: {}', requestURI, ExceptionUtils.getRootCauseMessage(exception))
    }

    @CompileStatic
    @Override
    void error(CSSParseException exception) throws CSSException {
        log.warn('error while parsing {}: {}', requestURI, ExceptionUtils.getRootCauseMessage(exception))
    }

    @CompileStatic
    @Override
    void fatalError(CSSParseException exception) throws CSSException {
        log.error('fatal error while parsing {}: {}', requestURI, ExceptionUtils.getRootCauseMessage(exception))
    }

}
