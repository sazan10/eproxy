package com.eaio.eproxy.rewriting.html

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import com.eaio.eproxy.rewriting.Rewriting

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class RemoveNoScriptElementsContentHandlerContentHandlerTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Test
    void '<noscript> elements should be removed'() {
        StringWriter output = new StringWriter()
        XMLReader reader = new Rewriting().newXMLReader()
        reader.contentHandler = new RemoveNoScriptElementsContentHandler(delegate: new HTMLSerializer(output))
        reader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/bla.html'))))
        errorCollector.checkThat(output as String, containsString('''<img width="1" height="1" onerror="alert('oh')" src="dah.jpg">


    


</body>''')) // Some whitespace in the HTML. :(
    }

}
