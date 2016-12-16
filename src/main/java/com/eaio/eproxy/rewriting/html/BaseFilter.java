package com.eaio.eproxy.rewriting.html;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.apache.xerces.xni.QName;
import org.cyberneko.html.filters.DefaultFilter;

/**
 * Base class for Xerces' {@link org.apache.xerces.xni.parser.XMLDocumentFilter document filters}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
public class BaseFilter extends DefaultFilter {

    /**    
     * @return if the tag is <code>expected</code>
     */
    public final boolean nameIs(QName qName, String expected) {
        return defaultIfNull(qName.localpart, qName.rawname).equalsIgnoreCase(expected);
    }

}
