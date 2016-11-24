package com.eaio.eproxy.rewriting.data_uri

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.codec.binary.Base64
import org.apache.http.HeaderElement
import org.apache.http.NameValuePair
import org.apache.http.message.BasicHeaderValueParser
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import com.eaio.eproxy.rewriting.Rewriting
import com.eaio.eproxy.rewriting.html.RewritingFilter
import com.eaio.stringsearch.BNDMCI

/**
 * Rewrites data: URIs where the MIME type is either an HTML or an SVG MIME type.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 * @see Rewriting#isHTML(String)
 * @see Rewriting#isSVG(String)
 */
@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
class DataURIFilter extends RewritingFilter implements BeanFactoryAware {

    @Lazy
    private BNDMCI bndmci = new BNDMCI()

    @Lazy
    private def patternData = bndmci.processString('data:')

    @Lazy
    private Rewriting rewriting = beanFactory.getBean(Rewriting)

    BeanFactory beanFactory

    @Autowired
    Base64 base64

    @Override
    void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) {
        rewriteElement(element, attributes, augs)
        super.startElement(element, attributes, augs)
    }

    @Override
    void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) {
        rewriteElement(element, attributes, augs)
        super.emptyElement(element, attributes, augs)
    }

    private void rewriteElement(QName qName, XMLAttributes atts, Augmentations augs) {
        int srcIndex = atts.getIndex('src')
        if (srcIndex >= 0I) {
            int dataIndex = bndmci.searchString(atts.getValue(srcIndex) ?: '', 'data:', patternData)
            if (dataIndex >= 0I) {
                rewriteDataURI(trimToEmpty(atts.getValue(srcIndex).substring(dataIndex + 5I)))
            }
        }
    }

    /**
     * Rewrites the data: URI value. The leading "data:" scheme must be removed.
     * 
     * @param dataURI the data: URI without the leading "data:" scheme
     * @return <code>null</code> if <code>dataURI</code> doesn't need to be rewritten
     */
    String rewriteDataURI(String dataURI) {
        HeaderElement[] elements = BasicHeaderValueParser.parseElements(dataURI, null)

        if (!needsRewriting(elements)) {
            return
        }

        String mimeType = getMIMEType(elements)
        boolean isHTML = rewriting.isHTML(mimeType), isSVG = rewriting.isSVG(mimeType)
        if (!isHTML && !isSVG) {
            return
        }
        
        boolean isBase64 = isBase64(elements)
        extractData(elements, isBase64)
    }
    
    String extractData(HeaderElement[] elements, boolean isBase64) {
        String fullData =  elements[elements.size() - 1I].value ?  elements[elements.size() - 1I].name + '=' +  elements[elements.size() - 1I].value :  elements[elements.size() - 1I].name
        URLDecoder.decode(isBase64 ? new String(base64.decode(fullData), 0I) : fullData)
    }
    
    /**
     * Returns if the data: URI contains only data or no MIME type (which defaults to text/plain according to Wikipedia)
     */
    boolean needsRewriting(HeaderElement[] elements) {
        elements.size() == 1I || elements[0I].name?.equalsIgnoreCase('base64') ? false : true
    }
    
    String getMIMEType(HeaderElement[] elements) {
        elements[0I].name
    }
    
    boolean isBase64(HeaderElement[] elements) {
        elements[0I].parameters?.any { NameValuePair pair -> pair.name?.equalsIgnoreCase('base64') }
    }

}
