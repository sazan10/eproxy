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
import com.eaio.net.httpclient.ReEncoding

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URIRewritingContentHandlerTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()
    
    @Test
    void 'view-source URIs should be rewritten'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newXMLReader()
        xmlReader.contentHandler = new URIRewritingContentHandler(reEncoding: new ReEncoding(), baseURI: 'http://rah.com/'.toURI(), requestURI: 'https://www.facebook.com/'.toURI(),
            rewriteConfig: new RewriteConfig(rewrite: true), delegate: new HTMLSerializer(output))
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        errorCollector.checkThat(output as String, containsString('view-source:http://rah.com/ah-http/auer-ha.com'))
        errorCollector.checkThat(output as String, not(containsString('view-source:https://fonts.googleapis.com')))
    }

}
