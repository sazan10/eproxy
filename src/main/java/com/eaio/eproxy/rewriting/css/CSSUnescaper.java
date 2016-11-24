package com.eaio.eproxy.rewriting.css;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

/**
 * Removes escape sequences in CSS.
 * <p>
 * Example: <code>\\000075 \\00072\\006C</code> =&gt; <code>url</code>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 * @see <a href="https://mathiasbynens.be/notes/css-escapes">https://mathiasbynens.be/notes/css-escapes</a>
 * @see <a href="https://mathiasbynens.be/demo/html5-id">https://mathiasbynens.be/demo/html5-id</a>
 */
public class CSSUnescaper extends CharSequenceTranslator {

    /**
     * @see org.apache.commons.lang3.text.translate.CharSequenceTranslator#translate(java.lang.CharSequence, int, java.io.Writer)
     */
    @Override
    public int translate(CharSequence input, int index, Writer out)
                    throws IOException {
        if (input.charAt(index) == '\\') {
            int remaining = input.length() - index - 1;
            if (remaining > 0) {
                int startOfEscape = index + 1;
                int endOfHex = startOfEscape;
                int endOfEscape = startOfEscape;
                char c;
                for (int i = 0; i < 7 /* 6 hex digits + whitespace */ && (startOfEscape + i) < input.length(); ++i) {
                    c = input.charAt(startOfEscape + i);
                    if (isHex(c)) {
                        ++endOfHex;
                        ++endOfEscape;
                        continue;
                    }
                    if (isWhitespace(c)) {
                        ++endOfEscape;
                        break;
                    }
                    break;
                }
                if (startOfEscape != endOfHex) {
                    out.write(Integer.parseInt(input.subSequence(startOfEscape, endOfHex).toString(), 16));
                }
                else if (endOfEscape > startOfEscape) {
                    out.write(input.subSequence(startOfEscape, endOfEscape).toString());
                }
                return endOfEscape - index;
            }
        }
        return 0;
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    private boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') ||
                        (c >= 'A' && c <= 'F');
    }

}
