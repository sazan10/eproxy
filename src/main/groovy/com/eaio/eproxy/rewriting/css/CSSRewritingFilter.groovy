package com.eaio.eproxy.rewriting.css

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.regex.Pattern

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString
import org.w3c.css.sac.*
import org.w3c.dom.css.*

import com.eaio.eproxy.rewriting.URIManipulation
import com.eaio.eproxy.rewriting.html.RewritingFilter
import com.steadystate.css.dom.*
import com.steadystate.css.parser.CSSOMParser
import com.steadystate.css.parser.SACParserCSS3

/**
 * Rewrites CSS using <a href="http://cssparser.sourceforge.net/">CSS Parser</a>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Slf4j
class CSSRewritingFilter extends RewritingFilter implements ErrorHandler, URIManipulation {
    
    @Lazy
    private Pattern patternURL = ~/(?i)(\75 ?|u)(\72 ?|r)(\6C ?|l)/
    
    private final Stack<String> stack = new Stack<String>()

    @CompileStatic
    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'style')) {
            stack.push('style')
        }
        else {
            String styleAttribute = atts.getValue('style') // TODO: SVG attributes (mask, fill and others?)
            if (styleAttribute && styleAttribute.length() > 8I) {
                String rewrittenStyleAttribute = rewriteStyleAttribute(new InputSource(characterStream: new StringReader(styleAttribute)))
                atts.setValue(atts.getIndex('style'), rewrittenStyleAttribute)
                log.debug('rewrote style attribute {} chars to {} chars', styleAttribute.length(), rewrittenStyleAttribute.length())
            }
        }
        super.startElement(qName, atts, augs)
    }

    @CompileStatic
    @Override
    void endElement(QName qName, Augmentations augs) {
        if (nameIs(qName, 'style')) {
            try {
                stack.pop()
            }
            catch (EmptyStackException ex) {}
        }
        super.endElement(qName, augs)
    }

    /**
     * Rewrites <tt>&lt;style&gt;</tt> contents.
     */
    @CompileStatic
    @Override
    void characters(XMLString xmlString, Augmentations augs) {
        String tag
        try {
            tag = stack.peek()
        }
        catch (EmptyStackException ex) {}
        if (tag == 'style' && xmlString.length > 11I) {
            DirectStrBuilder builder = new DirectStrBuilder(xmlString.length)
            Reader charArrayReader = new CharArrayReader(xmlString.ch, xmlString.offset, xmlString.length)
            rewriteCSS(new InputSource(characterStream: charArrayReader), builder.asWriter())
            super.characters(new XMLString(builder.buffer, 0I, builder.length()), augs)
            log.debug('rewrote CSS {} chars to {} chars', xmlString.length, builder.length())
        }
        else {
            super.characters(xmlString, augs)
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
            rule.href = encodeTargetURI(baseURI, requestURI, rule.href, rewriteConfig)
        }
        else if (rule instanceof CSSUnknownRuleImpl) {
            if (patternURL.matcher(rule.text).find()) {
                log.warn('url in unknown CSS rule in {}: {}', requestURI, rule.text)
            }
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
        else if (containsURILexicalUnit(value) && value.value?.stringValue && attributeValueNeedsRewriting(value.value.stringValue)) {
            value.value.stringValue = encodeTargetURI(baseURI, requestURI, value.value.stringValue, rewriteConfig)
        }
        else if (containsFunctionLexicalUnit(value) && value.value?.parameters?.stringValue && attributeValueNeedsRewriting(value.value.parameters.stringValue)) {
            value.value.parameters.stringValue = encodeTargetURI(baseURI, requestURI, value.value.parameters.stringValue, rewriteConfig)
        }
    }

    @CompileStatic
    boolean containsURILexicalUnit(CSSValueImpl value) {
        value.value instanceof LexicalUnit && ((LexicalUnit) value.value).lexicalUnitType == LexicalUnit.SAC_URI
    }
    
    @CompileStatic
    boolean containsFunctionLexicalUnit(CSSValueImpl value) {
        value.value instanceof LexicalUnit && ((LexicalUnit) value.value).lexicalUnitType == LexicalUnit.SAC_FUNCTION
    }

    @CompileStatic
    @Override
    void warning(CSSParseException exception) throws CSSException {
        log.info('while parsing {}@{}:{}: {}', requestURI, exception.lineNumber, exception.columnNumber, (ExceptionUtils.getRootCause(exception) ?: exception).message)
    }

    @CompileStatic
    @Override
    void error(CSSParseException exception) throws CSSException {
        log.warn('error while parsing {}@{}:{}: {}', requestURI, exception.lineNumber, exception.columnNumber, (ExceptionUtils.getRootCause(exception) ?: exception).message)
    }

    @CompileStatic
    @Override
    void fatalError(CSSParseException exception) throws CSSException {
        log.error('fatal error while parsing {}@{}:{}: {}', requestURI, exception.lineNumber, exception.columnNumber, (ExceptionUtils.getRootCause(exception) ?: exception).message)
    }

}
