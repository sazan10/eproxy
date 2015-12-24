package com.eaio.eproxy.rewriting

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.ccil.cowan.tagsoup.Parser
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.xml.sax.InputSource

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
        Parser parser = new Parser()
        parser.contentHandler = new RemoveNoScriptElementsContentHandler(new HTMLSerializer(output))
        parser.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/bla.html'))))
        errorCollector.checkThat(output as String, containsString('''<img width="1" height="1" onerror="alert('oh')" src="dah.jpg">


    


</body>''')) // Some whitespace in the HTML. :(
    }

}
