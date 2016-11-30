package com.eaio.eproxy.rewriting.data_uri

import static org.apache.commons.lang3.StringUtils.*

import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

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
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Base64

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
            rewriteAttributeValue(atts, srcIndex)
        }
    }

    private rewriteAttributeValue(XMLAttributes atts, int srcIndex) {
        String value = trimToEmpty(atts.getValue(srcIndex))
        if (startsWithIgnoreCase(value, 'data:')) {
            String dataURIValue = trimToEmpty(value.substring(5I))
            HeaderElement[] elements = BasicHeaderValueParser.parseElements(dataURIValue, null)

            if (isMissingMIMEType(elements)) {
                return
            }

            String mimeType = getMIMEType(elements)
            boolean isHTML = rewriting.isHTML(mimeType), isSVG = rewriting.isSVG(mimeType)
            if (!isHTML && !isSVG) {
                return
            }

            Charset charset = getCharset(elements)
            boolean base64 = isBase64(elements)
            String data = extractData(elements, base64)

            log.info('data: URI of type {}, charset {}, base64 {}: {}. Extracted data: {}', mimeType, charset, base64, dataURIValue, data)

            String rewritten = rewriteData(data, mimeType, charset)
            atts.setValue(srcIndex, 'data:' + mimeType + ',' + URLEncoder.encode(rewritten))
        }
    }

    String rewriteData(String data, String mimeType, Charset charset) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(data.length())
        rewriting.rewrite(new ByteArrayInputStream(data.getBytes(charset?.name() ?: 'UTF-8')), bOut, charset, baseURI, requestURI, rewriteConfig, mimeType)
        bOut.toString(0I)
    }

    /**
     * Extracts the data: URI value.
     * 
     * @param isBase64 whether the data is Base64 encoded
     */
    String extractData(HeaderElement[] elements, boolean isBase64) {
        String fullData =  elements[elements.size() - 1I].value ?  elements[elements.size() - 1I].name + '=' +  elements[elements.size() - 1I].value :  elements[elements.size() - 1I].name
        URLDecoder.decode(isBase64 ? new String(base64.decode(fullData), 0I) : fullData)
    }

    /**
     * Returns if the data: URI contains only data or no MIME type (which defaults to text/plain according to Wikipedia)
     */
    boolean isMissingMIMEType(HeaderElement[] elements) {
        elements.size() == 1I || elements[0I].name?.equalsIgnoreCase('base64') ? true : false
    }

    String getMIMEType(HeaderElement[] elements) {
        elements[0I].name
    }

    Charset getCharset(HeaderElement[] elements) {
        Charset out
        String charsetName = elements[0I].parameters?.find { NameValuePair pair -> pair.name?.equalsIgnoreCase('charset') }?.value
        if (charsetName) {
            try {
                out = Charset.forName(charsetName)
            }
            catch (IllegalCharsetNameException ex) {
                log.warn('illegal charset name {}', charsetName)
            }
        }
        out
    }

    boolean isBase64(HeaderElement[] elements) {
        elements[0I].name?.equalsIgnoreCase('base64') || elements[0I].parameters?.any { NameValuePair pair -> pair.name?.equalsIgnoreCase('base64') }
    }

}
