package com.eaio.eproxy.rewriting.html

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.apache.xerces.xni.parser.XMLDocumentFilter
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
class RemoveActiveContentFilterTest {
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector()

    @Test
    void 'active content and on* handlers should be removed'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newHTMLReader()
        XMLDocumentFilter[] filters = [ new RemoveActiveContentFilter(),
            new org.cyberneko.html.filters.Writer(output, 'UTF-8') ].toArray()
        xmlReader.setProperty('http://cyberneko.org/html/properties/filters', filters)
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/bla.html'))))
        errorCollector.checkThat(output as String, allOf(not(containsString('<script')), not(containsString('<SCRIPT'))))
        errorCollector.checkThat(output as String, containsString('<IMG src="fnuh.jpg">'))
        errorCollector.checkThat(output as String, containsString('<IMG width="1" height="1" src="dah.jpg">'))
        errorCollector.checkThat(output as String, allOf(not(containsString('<rah ')), not(containsString('<RAH'))))
    }

}
