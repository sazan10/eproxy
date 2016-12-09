package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.junit.Test
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import com.eaio.eproxy.rewriting.Rewriting

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class ScriptFilterTest {
    
    @Test
    void 'script should only be appended once'() {
        StringWriter output = new StringWriter()
        XMLReader xmlReader = new Rewriting().newHTMLReader()
        XMLDocumentFilter[] filters = [ new ScriptFilter(scriptRedirect: 'http://blorb.com'),
            new org.cyberneko.html.filters.Writer(output, 'UTF-8') ].toArray()
        xmlReader.setProperty('http://cyberneko.org/html/properties/filters', filters)
        xmlReader.parse(new InputSource(characterStream: new FileReader(new File('src/test/resources/com/eaio/eproxy/rewriting/html/https_www.unrealengine.com_what-is-unreal-engine-4.html'))))
        assertThat(countMatches(output as String, 'src="/script'), is(1I))
    }

}
