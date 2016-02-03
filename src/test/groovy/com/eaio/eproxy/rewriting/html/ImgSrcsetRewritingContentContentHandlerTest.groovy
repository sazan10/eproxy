package com.eaio.eproxy.rewriting.html

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting
import com.eaio.net.httpclient.ReEncoding

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class ImgSrcsetRewritingContentContentHandlerTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Test
    void '<img srcset> should be rewritten'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newXMLReader()
        XMLDocumentFilter[] filters = [ new ImgSrcsetRewritingContentHandler(reEncoding: new ReEncoding(), baseURI: 'http://rah.com'.toURI(),
            requestURI: 'https://plop.com/ui.html?fnuh=guh'.toURI(), rewriteConfig: new RewriteConfig(rewrite: true)),
            new org.cyberneko.html.filters.Writer(output, 'UTF-8') ].toArray()
        xmlReader.setProperty('http://cyberneko.org/html/properties/filters', filters)
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        errorCollector.checkThat(output as String, containsString('view-source:http://rah.com/ah-http/fnuh.com/creme.jpg'))
        errorCollector.checkThat(output as String, containsString(',http://rah.com/ah-https/plop.com/fnord.jpg 640w,'))
    }

}
