package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import com.eaio.eproxy.rewriting.URIManipulation

/**
 * Rewrites <tt>&lt;meta refresh&gt;</tt>. Should be placed after {@link RemoveNoScriptElementsFilter}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MetaRewritingFilter extends RewritingFilter implements URIManipulation {
    
    @Override
    void startElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.startElement(qName, atts, augs)
    }

    @Override
    void emptyElement(QName qName, XMLAttributes atts, Augmentations augs) {
        rewriteElement(qName, atts, augs)
        super.emptyElement(qName, atts, augs)
    }
    
    private void rewriteElement(QName qName, XMLAttributes atts, Augmentations augs) {
        if (nameIs(qName, 'meta')) {
            String httpEquiv = atts.getValue('http-equiv')
            if (httpEquiv) {
                String content = atts.getValue('content')
                if (content && containsIgnoreCase(httpEquiv, 'refresh') && containsIgnoreCase(content, 'url')) {
                    String rewritten = rewriteMetaRefresh(content)
                    atts.setValue(atts.getIndex('content'), rewritten)
                }
            }
        }
    }
    
    /**
     * @param metaRefresh something like <tt>0; url=http://pruh.com</tt>
     * @return the same string with the URL rewritten
     * @see URIManipulation#encodeTargetURI(URI, URI, String, RewriteConfig)
     */
    String rewriteMetaRefresh(String metaRefresh) {
        HeaderElement[] elements = BasicHeaderValueParser.parseElements(metaRefresh, null)
        String url = elements[0I]?.getParameterByName('URL')?.value
        if (url) {
            url = removeStart(url, '"')
            url = removeStart(url, '\'')
            url = removeEnd(url, '"')
            url = removeEnd(url, '\'')
            replace(metaRefresh, url, encodeTargetURI(baseURI, requestURI, url, rewriteConfig))
        }
        else {
            metaRefresh
        }
    }

}
