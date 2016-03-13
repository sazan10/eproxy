package com.eaio.eproxy.rewriting.css;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
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
public class CSSEscapingUtils {

    private static final String[][] CSS_UNESCAPE = new String[][] {
        { "&lpar;", "(" },
        { "&rpar;", ")" }
    };
    
    public CharSequenceTranslator UNESCAPE_CSS = new AggregateTranslator(new LookupTranslator(CSS_UNESCAPE),
         new CSSUnescaper());

    public CharSequence unescapeCSS(CharSequence input) {
        return UNESCAPE_CSS.translate(input);
    }

}
