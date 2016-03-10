package com.eaio.eproxy.rewriting.css

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.regex.Pattern

import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString
import org.w3c.css.sac.*
import org.w3c.dom.css.*

import com.eaio.eproxy.rewriting.URIManipulation
import com.eaio.eproxy.rewriting.html.RewritingFilter
import com.steadystate.css.dom.*

/**
 * Rewrites CSS using regular expressions because there are no robust Java CSS parsers.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Slf4j
class CSSRewritingFilter extends RewritingFilter implements URIManipulation {
    
    @Lazy
    private static final List<Pattern> replacements = Collections.unmodifiableList([
        ~/(?i)(?:(?:\\75 ?|u)(?:\\72 ?|r)(?:\\6C ?|l)|image)\s*\(\s*(["']([^#][^"']+)["']|([^#][^\s)]+))/, // TODO: Escape
        ~/(?i)@import\s*(?:["']([^#][^"']+)["']|([^#][^\s;]+))/,
        ~/(?i)\W(?:src|colorSpace)\s*=\s*(?:["']([^#][^"']+)["']|([^#][^\s)]+))/
        ])
    
    private boolean inStyleElement

    /**
     * Rewrites any style attributes, too.
     * 
     * @see org.cyberneko.html.filters.DefaultFilter#startElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
     */
    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'style')) {
            inStyleElement = true
        }
        else {
            String styleAttribute = atts.getValue('style') // TODO: SVG attributes (mask, fill and others?)
            if (isNotBlank(styleAttribute)) {
                styleAttribute = CSSEscapeUtils.unescapeCSS(styleAttribute)
                DirectStrBuilder builder = new DirectStrBuilder(styleAttribute.length())
                rewriteCSS(styleAttribute, builder.asWriter())
                atts.setValue(atts.getIndex('style'), builder.toString())
                log.debug('rewrote style attribute {} chars to {} chars', styleAttribute.length(), builder.length())
            }
        }
        super.startElement(qName, atts, augs)
    }

    @Override
    void endElement(QName qName, Augmentations augs) {
        if (nameIs(qName, 'style')) {
            inStyleElement = false
        }
        super.endElement(qName, augs)
    }

    /**
     * Rewrites <tt>&lt;style&gt;</tt> contents.
     * 
     * @see org.cyberneko.html.filters.DefaultFilter#characters(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    @Override
    void characters(XMLString xmlString, Augmentations augs) {
        if (inStyleElement) {
            String css = new String(xmlString.ch, xmlString.offset, xmlString.length) // TODO: performance
            if (isNotBlank(css)) {
                DirectStrBuilder builder = new DirectStrBuilder(xmlString.length)
                rewriteCSS(css, builder.asWriter())
                super.characters(new XMLString(builder.buffer, 0I, builder.length()), augs)
                log.debug('rewrote CSS {} chars to {} chars', xmlString.length, builder.length())
            }
        }
        else {
            super.characters(xmlString, augs)
        }
    }

    void rewriteCSS(String css, Writer writer) {
        String out = replacements.inject(css, { String s, Pattern p ->
            s.replaceAll(p, { List<String> matches ->
                String out = matches[0I]
                String uri = matches[2I] ?: matches[1I]
                if (attributeValueNeedsRewriting(uri)) {
                    String rewritten = encodeTargetURI(baseURI, requestURI, uri, rewriteConfig)
                    out = replace(matches[0I], uri, rewritten)
                }
                out
            })
        })
        writer.write(out)
    }

}
