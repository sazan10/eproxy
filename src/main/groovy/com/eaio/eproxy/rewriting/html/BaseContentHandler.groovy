package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import static org.apache.commons.lang3.reflect.FieldUtils.*
import groovy.transform.CompileStatic

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes
import org.cyberneko.html.filters.DefaultFilter
import org.xml.sax.Attributes

/**
 * Base class for SAX 2 {@link ContentHandler ContentHandlers}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
@CompileStatic
class BaseContentHandler extends DefaultFilter {
    
    final Stack<String> stack = new Stack<String>()
            
    boolean nameIs(QName qName, String expected) {
        equalsIgnoreCase(qName.localpart ?: qName.rawname, expected)
    }

}
