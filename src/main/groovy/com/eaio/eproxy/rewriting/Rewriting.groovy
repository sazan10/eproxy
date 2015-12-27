package com.eaio.eproxy.rewriting

import groovy.util.logging.Slf4j

import java.nio.charset.Charset

import org.cyberneko.html.parsers.SAXParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.css.CSSRewritingContentHandler
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
    
    @Autowired
    SupportedMIMETypes supportedMIMETypes
    
    boolean canRewrite(RewriteConfig rewriteConfig, String mimeType) {
        // TODO: Look at Content-Disposition header to prevent downloads from being rewritten
        rewriteConfig && supportedMIMETypes.isHTML(mimeType)// || supportedMIMETypes.isCSS(mimeType))
    }
    
    void rewrite(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig, String mimeType) {
        if (supportedMIMETypes.isHTML(mimeType)) {
            rewriteHTML(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
        else if (supportedMIMETypes.isCSS(mimeType)) {
            rewriteCSS(inputStream, outputStream, charset, baseURI, requestURI, rewriteConfig)
        }
    }
    
    void rewriteHTML(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset)
        XMLReader xmlReader = newXMLReader()
        try {
            xmlReader.contentHandler = new CSSRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: new RewriteConfig(rewrite: true), delegate:
                new MetaRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: new RewriteConfig(rewrite: true), delegate:
                    new RemoveActiveContentContentHandler(delegate:
                        new RemoveNoScriptElementsContentHandler(delegate:
                            new URIRewritingContentHandler(baseURI: baseURI, requestURI: requestURI, rewriteConfig: new RewriteConfig(rewrite: true), delegate:
                                    new HTMLSerializer(outputWriter)
                                    )
                                )
                            )
                        )
                    )
            xmlReader.parse(new InputSource(new InputStreamReader(inputStream, charset)))
        }
        catch (SAXException ex) {
            log.warn("While parsing {}@{}:{}", requestURI, ((DelegatingContentHandler) xmlReader.contentHandler).documentLocator.lineNumber,
                 ((DelegatingContentHandler) xmlReader.contentHandler).documentLocator.columnNumber, ex)
            throw ex
        }
        finally {
            outputWriter.flush()
        }
    }
    
    void rewriteCSS(InputStream inputStream, OutputStream outputStream, Charset charset, URI baseURI, URI requestURI, RewriteConfig rewriteConfig) {
        Writer outputWriter = new OutputStreamWriter(outputStream, charset)
        try {
            
        }
        finally {
            
        }
    }
    
    XMLReader newXMLReader() {
        //new Parser() // TagSoup
        SAXParser out = new SAXParser()
        out.setFeature('http://cyberneko.org/html/features/balance-tags', false)
        out
    }

}
