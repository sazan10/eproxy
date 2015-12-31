package com.eaio.eproxy.rewriting.html

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting

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
        xmlReader.contentHandler = new ImgSrcsetRewritingContentHandler(baseURI: 'http://rah.com'.toURI(),
            requestURI: 'https://plop.com/ui.html?fnuh=guh'.toURI(), rewriteConfig: new RewriteConfig(rewrite: true), delegate: new HTMLSerializer(output))
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        errorCollector.checkThat(output as String, not(containsString('http://fnuh.com/creme.jpg')))
        errorCollector.checkThat(output as String, containsString(',http://rah.com/ah-https/plop.com/fnord.jpg 640w,'))
    }

}
