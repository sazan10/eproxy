package com.eaio.eproxy.rewriting.css;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
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

    private static final String[][] HTML_UNESCAPE = new String[][] {
        { "&lpar;", "(" },
        { "&rpar;", ")" }
    };

    private static CharSequenceTranslator UNESCAPE_HTML = new LookupTranslator(HTML_UNESCAPE);

    /**
     * Unescapes HTML that NekoHTML doesn't.
     * 
     * @param input may be <code>null</code>
     */
    public static CharSequence unescapeHTML(CharSequence input) {
        return UNESCAPE_HTML.translate(input);
    }

}
