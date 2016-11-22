package com.eaio.eproxy.rewriting

import static org.apache.commons.io.IOUtils.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.charset.Charset

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.HeaderElement
import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.apache.xml.serialize.*
import org.cyberneko.html.filters.DefaultFilter
import org.cyberneko.html.parsers.SAXParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.XMLReaderFactory

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.css.*
import com.eaio.eproxy.rewriting.html.*
import com.eaio.eproxy.rewriting.svg.SVGFilter

/**
 * Rewrites links in different MIME types.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Component
@Slf4j
class Rewriting {

    @Autowired(required = false)
    ProxyJavaScriptFilter proxyJavaScriptFilter
    
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

    @Lazy
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
        rewriteConfig && !(contentDisposition?.name?.equalsIgnoreCase('attachment')) && (isHTML(mimeType) || isCSS(mimeType) || isSVG(mimeType))
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
        XMLReader xmlReader = newHTMLReader(outputWriter, charset, baseURI, requestURI, rewriteConfig)
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
        catch (NullPointerException ignored) {}
        finally {
            try {
                outputWriter.flush()
            }
            catch (emall) {}
        }
    }
    
    void rewriteHTMLFragment(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, (Charset) charset ?: defaultCharset)
        XMLReader xmlReader = newHTMLFragmentReader(outputWriter, charset, baseURI, requestURI, rewriteConfig)
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
        catch (NullPointerException ignored) {}
        finally {
            try {
                outputWriter.flush()
            }
            catch (emall) {}
        }
    }

    void rewriteCSS(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset ?: defaultCharset)
        CSSRewritingFilter handler = configure(new CSSRewritingFilter(), baseURI, requestURI, rewriteConfig)
        try {
            outputWriter.write(handler.rewriteCSS(toString(inputStream, charset ?: defaultCharset)))
        }
        catch (NullPointerException ignored) {}
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
        // 1st in chain
        DefaultFilter cssRewritingFilter = configure(new CSSRewritingFilter(), baseURI, requestURI, rewriteConfig)
        xmlReader.contentHandler = new ContentHandlerXMLDocumentHandlerAdapter(cssRewritingFilter)
        // 2nd in chain
        DefaultFilter uriRewritingFilter = configure(new URIRewritingFilter(), baseURI, requestURI, rewriteConfig)
        cssRewritingFilter.documentHandler = uriRewritingFilter
        // 3rd in chain
        XMLSerializer serializer = new XMLSerializer(outputWriter, new OutputFormat(Method.XML, (charset ?: defaultCharset).name(), true))
        uriRewritingFilter.documentHandler = new XMLDocumentHandlerDocumentHandlerAdapter(serializer)
        try {
            xmlReader.parse(newSAXInputSource(inputStream, charset))
        }
        catch (NullPointerException ignored) {}
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
    
    XMLReader newHTMLReader(Writer outputWriter, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        XMLReader out = newHTMLReader()
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
                configure(new URIRewritingFilter(), baseURI, requestURI, rewriteConfig),
            ])
            
            RecursiveInlineHTMLRewritingFilter recursiveInlineHTMLRewritingFilter = configure(new RecursiveInlineHTMLRewritingFilter(), baseURI, requestURI, rewriteConfig)
            recursiveInlineHTMLRewritingFilter.rewriting = new Rewriting() // Needs to be a new reference for unclear reasons
            filters << recursiveInlineHTMLRewritingFilter
            
            if (proxyJavaScriptFilter) {
                filters << proxyJavaScriptFilter
            }
        }
        filters << new SVGFilter() << new org.cyberneko.html.filters.Writer(outputWriter, (charset ?: defaultCharset).name())
        out.setProperty('http://cyberneko.org/html/properties/filters', (XMLDocumentFilter[]) filters.toArray())
        out
    }
    
    XMLReader newHTMLFragmentReader(Writer outputWriter, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        XMLReader out = newHTMLReader(outputWriter, charset, baseURI, requestURI, rewriteConfig)
        out.setFeature('http://cyberneko.org/html/features/balance-tags/document-fragment', true)
        out
    }
    
    XMLReader newXMLReader() {
        XMLReader out = XMLReaderFactory.createXMLReader()
        out.with {
            setFeature('http://xml.org/sax/features/namespace-prefixes', true)
            // Prevent external XML entities from deadlocking threads, see doc/deadlock-external-xml-entity.txt
            setFeature('http://xml.org/sax/features/external-general-entities', false)
            setFeature('http://xml.org/sax/features/external-parameter-entities', false)
        }
        out
    }

    InputSource newSAXInputSource(InputStream inputStream, Charset charset) {
        InputSource out = new InputSource(byteStream: inputStream)
        if (charset) {
            out.encoding = charset.displayName()
        }
        out
    }
    
    private <T extends RewritingFilter> T configure(T filter, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        filter.baseURI = baseURI
        filter.requestURI = requestURI
        filter.rewriteConfig = rewriteConfig
        filter
    }
    
}
