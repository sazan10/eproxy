package com.eaio.eproxy.rewriting

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.charset.Charset

import javax.xml.parsers.SAXParserFactory

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.HeaderElement
import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.apache.xml.serialize.*
import org.cyberneko.html.filters.DefaultFilter
import org.cyberneko.html.parsers.SAXParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.XMLReaderFactory

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.css.*
import com.eaio.eproxy.rewriting.html.*
import com.eaio.net.httpclient.ReEncoding

/**
 * Rewriting code for different MIME types.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Slf4j
class Rewriting {

    @Autowired
    ReEncoding reEncoding

    @Autowired
    TelemetryFilter telemetryFilter    

    @Lazy
    private Charset defaultCharset = Charset.forName('UTF-8')

    //    private Set<String> javascript = [
    //        'application/ecmascript',
    //        'application/javascript',
    //        'application/x-ecmascript',
    //        'application/x-javascript',
    //        'text/ecmascript',
    //        'text/javascript',
    //        'text/javascript1.0',
    //        'text/javascript1.1',
    //        'text/javascript1.2',
    //        'text/javascript1.3',
    //        'text/javascript1.4',
    //        'text/javascript1.5',
    //        'text/jscript',
    //        'text/livescript',
    //        'text/x-ecmascript',
    //        'text/x-javascript',
    //    ] as Set

    // TODO: Get rid of VBScript

    private Set<String> html = [
        'text/html',
        'text/x-server-parsed-html',
        'application/xml+xhtml',
    ] as Set

    boolean isHTML(String mimeType) {
        html.contains(mimeType?.toLowerCase() ?: '')
    }

    boolean isCSS(String mimeType) {
        mimeType?.equalsIgnoreCase('text/css')
    }

    boolean isSVG(String mimeType) {
        mimeType?.equalsIgnoreCase('image/svg+xml')
    }

    boolean canRewrite(HeaderElement contentDisposition, RewriteConfig rewriteConfig, String mimeType) {
        !(contentDisposition?.name?.equalsIgnoreCase('attachment')) && rewriteConfig && (isHTML(mimeType) || isCSS(mimeType) || isSVG(mimeType))
    }

    void rewrite(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig, String mimeType) {
        if (isHTML(mimeType)) {
            rewriteHTML(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
        else if (isCSS(mimeType)) {
            rewriteCSS(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
        else if (isSVG(mimeType)) {
            rewriteSVG(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
    }

    void rewriteHTML(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, (Charset) charset ?: defaultCharset)
        XMLReader xmlReader = newHTMLReader()
        Collection<DefaultFilter> filters = []
        if (rewriteConfig.removeActiveContent) {
            filters << new RemoveActiveContentFilter()
        }
        if (rewriteConfig.removeNoScriptElements) {
            filters << new RemoveNoScriptElementsFilter()
        }
        if (rewriteConfig.rewrite) {
            filters.addAll([
                configure(new CSSRewritingFilter(), baseURI, requestURI, rewriteConfig),
                configure(new MetaRewritingFilter(), baseURI, requestURI, rewriteConfig),
                configure(new SrcsetFilter(), baseURI, requestURI, rewriteConfig),
                configure(new URIRewritingFilter(), baseURI, requestURI, rewriteConfig)
            ])
        }
        filters << telemetryFilter << new org.cyberneko.html.filters.Writer(outputWriter, (charset ?: defaultCharset).name())
        xmlReader.setProperty('http://cyberneko.org/html/properties/filters', (XMLDocumentFilter[]) filters.toArray())
        try {
            xmlReader.parse(newSAXInputSource(inputStream, charset))
        }
        catch (SAXParseException ex) {
            log.warn("While parsing {}@{}:{}: {}", requestURI, ex.lineNumber, ex.columnNumber, (ExceptionUtils.getRootCause(ex) ?: ex).message)
        }
        catch (SAXException ex) {
            if (ExceptionUtils.getRootCause(ex) instanceof IOException) {
                throw ExceptionUtils.getRootCause(ex)
            }
            else {
                log.warn("While parsing {}: {}", requestURI, (ExceptionUtils.getRootCause(ex) ?: ex).message)
            }
        }
        finally {
            try {
                outputWriter.flush()
            }
            catch (emall) {}
        }
    }

    void rewriteCSS(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset ?: defaultCharset)
        try {
            CSSRewritingFilter handler = configure(new CSSRewritingFilter(), baseURI, requestURI, rewriteConfig)
            handler.rewriteCSS(newSACInputSource(inputStream, charset), outputWriter)
        }
        finally {
            try {
                outputWriter.flush()
            }
            catch (emall) {}
        }
    }
    
    void rewriteSVG(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset ?: defaultCharset)
        XMLReader xmlReader = newXMLReader()
        xmlReader.contentHandler = new XMLSerializer(outputWriter, new OutputFormat(Method.XML, charset.name(), true))
        try {
            xmlReader.parse(newSAXInputSource(inputStream, charset))
        }
        finally {
            try {
                outputWriter.flush()
            }
            catch (emall) {}
        }
    }

    XMLReader newHTMLReader() {
        SAXParser out = new SAXParser()
        out.setFeature('http://cyberneko.org/html/features/balance-tags', false)
        out.setProperty('http://cyberneko.org/html/properties/names/elems', 'lower')
        out
    }
    
    XMLReader newXMLReader() {
        XMLReaderFactory.newInstance().createXMLReader()
    }

    InputSource newSAXInputSource(InputStream inputStream, Charset charset) {
        InputSource out = new InputSource(byteStream: inputStream)
        if (charset) {
            out.encoding = charset.displayName()
        }
        out
    }

    org.w3c.css.sac.InputSource newSACInputSource(InputStream inputStream, Charset charset) {
        org.w3c.css.sac.InputSource out = new org.w3c.css.sac.InputSource(byteStream: inputStream)
        if (charset) {
            out.encoding = charset.displayName()
        }
        out
    }
    
    private <T extends RewritingFilter> T configure(T filter, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        filter.reEncoding = reEncoding
        filter.baseURI = baseURI
        filter.requestURI = requestURI
        filter.rewriteConfig = rewriteConfig
        filter
    }
    
}
