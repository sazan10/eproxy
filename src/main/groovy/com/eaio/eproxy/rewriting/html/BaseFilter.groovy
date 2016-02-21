package com.eaio.eproxy.rewriting.html

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.cyberneko.html.filters.DefaultFilter

/**
 * Base class for Xerces' {@link org.apache.xerces.xni.parser.XMLDocumentFilter document filters}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
@CompileStatic
class BaseFilter extends DefaultFilter {
            
    final boolean nameIs(QName qName, String expected) {
        equalsIgnoreCase(qName.localpart ?: qName.rawname, expected)
    }

}
