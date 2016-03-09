package com.eaio.eproxy.rewriting.css;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * <del>Stolen from</del> <ins>Loosely following</ins> {@link StringEscapeUtils}.
 * (Un)escapes some things that NekoHTML apparently doesn't.
 * <p>
 * Not really well tested.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class CSSEscapeUtils {

    private static final String[][] CSS_UNESCAPE = new String[][] {
        { "&lpar;", "(" },
        { "&rpar;", ")" }
    };
    
    private static final String[][] CSS_ESCAPE = EntityArrays.invert(CSS_UNESCAPE);
    
    public static final CharSequenceTranslator ESCAPE_CSS =
            new LookupTranslator(CSS_ESCAPE);
    
    public static final CharSequenceTranslator UNESCAPE_CSS = 
            new LookupTranslator(CSS_UNESCAPE);

    public static final String unescapeCSS(final String input) {
        return UNESCAPE_CSS.translate(input);
    }
    
    public static final String escapeCSS(final String input) {
        return ESCAPE_CSS.translate(input);
    }

}
