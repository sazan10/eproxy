package com.eaio.eproxy.rewriting

import groovy.util.logging.Slf4j

import java.nio.charset.Charset

import org.apache.commons.lang3.exception.ExceptionUtils
import org.cyberneko.html.parsers.SAXParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.css.*
import com.eaio.eproxy.rewriting.html.*

/**
 * Rewriting code for different MIME types.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Component
@Slf4j
class Rewriting {
    
    Set<String> javascript = [
        'application/ecmascript',
        'application/javascript',
        'application/x-ecmascript',
        'application/x-javascript',
        'text/ecmascript',
        'text/javascript',
        'text/javascript1.0',
        'text/javascript1.1',
        'text/javascript1.2',
        'text/javascript1.3',
        'text/javascript1.4',
        'text/javascript1.5',
        'text/jscript',
        'text/livescript',
        'text/x-ecmascript',
        'text/x-javascript',
    ] as Set

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
    
    // TODO: Look at Content-Disposition header to prevent downloads from being rewritten
    boolean canRewrite(String contentDisposition, RewriteConfig rewriteConfig, String mimeType) {
        rewriteConfig && (isHTML(mimeType) || isCSS(mimeType))
    }
    
    void rewrite(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig, String mimeType) {
        if (isHTML(mimeType)) {
            rewriteHTML(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
        else if (isCSS(mimeType)) {
            rewriteCSS(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
    }
    
    void rewriteHTML(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset ?: Charset.forName('UTF-8'))
        XMLReader xmlReader = newXMLReader()
        try {
            xmlReader.contentHandler = new CSSRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: rewriteConfig, delegate:
                new MetaRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: rewriteConfig, delegate:
                    new RemoveActiveContentContentHandler(delegate:
                        new RemoveNoScriptElementsContentHandler(delegate:
                            new ImgSrcsetRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: rewriteConfig, delegate:
                                new URIRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: rewriteConfig, delegate:
                                        new HTMLSerializer(outputWriter)
                                        )
                                    )
                                )
                            )
                        )
                    )
            xmlReader.parse(new InputSource(new InputStreamReader(inputStream, charset ?: Charset.forName('UTF-8')))) // TODO: BufferedInputStream?
        }
        catch (SAXException ex) {
            if (ExceptionUtils.getRootCause(ex) instanceof IOException) {
                throw ExceptionUtils.getRootCause(ex)
            }
            else {
                log.warn("While parsing {}@{}:{}", requestURI, ((DelegatingContentHandler) xmlReader.contentHandler).documentLocator.lineNumber,
                    ((DelegatingContentHandler) xmlReader.contentHandler).documentLocator.columnNumber, ex)
            }
        }
        finally {
            try {
                outputWriter.flush()
            }
            catch (IOException ignored) {}
        }
    }
    
    void rewriteCSS(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset ?: Charset.forName('UTF-8'))
        try {
            new CSSRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: rewriteConfig)
                .rewriteCSS(new InputStreamReader(inputStream, charset ?: Charset.forName('UTF-8')), outputWriter)
        }
        finally {
            try {
                outputWriter.flush()
            }
            catch (IOException ignored) {}
        }
    }
    
    XMLReader newXMLReader() {
        SAXParser out = new SAXParser()
        out.setFeature('http://cyberneko.org/html/features/balance-tags', false)
        out
    }

}
