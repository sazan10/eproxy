package com.eaio.eproxy.rewriting.html

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import com.eaio.eproxy.api.Proxy
import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.Rewriting

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class MetaRewritingContentHandlerTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()
    
    @Lazy
    MetaRewritingContentHandler contentHandler

    @Test
    void '<meta refresh> should be rewritten'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newXMLReader()
        xmlReader.contentHandler = new MetaRewritingContentHandler(baseURI: 'http://rah.com/'.toURI(), requestURI: 'https://www.facebook.com/'.toURI(),
            rewriteConfig: new RewriteConfig(rewrite: true), delegate: new HTMLSerializer(output))
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        errorCollector.checkThat(output as String, containsString('<meta http-equiv="refresh" content="5; url=http://rah.com/ah-https/www.facebook.com/blorb.html"'))
    }
    
    @Test
    void 'Baidu\'s <meta refresh> should be rewritten'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newXMLReader()
        xmlReader.contentHandler = new MetaRewritingContentHandler(baseURI: 'http://rah.com/'.toURI(), requestURI: 'https://www.facebook.com/'.toURI(),
            rewriteConfig: new RewriteConfig(rewrite: true), delegate: new HTMLSerializer(output))
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/baidu-redirect.html'))))
        errorCollector.checkThat(output as String, containsString('<meta http-equiv="refresh" content="0;URL=http://rah.com/ah-http/www.n-tv.de/"'))
    }

}
