package com.eaio.eproxy.rewriting.css;

import java.io.IOException;

/**
 * Creates regular expressions that support CSS escaping.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class CSSEscapeRegEx {
    
    public static Appendable appendCharacter(Appendable self, Character o) throws IOException {
        return appendCharacter(self, o, false);
    }
    
    public static Appendable appendCharacter(Appendable self, Character o, boolean escape) throws IOException {
        self.append("(?:\\\\");
        int c = (int) o.charValue();
        int zeroes = c < 256 ? 4 : c < 65536 ? 2 : 0;
        if (zeroes > 0) {
            self.append("0{0,");
            self.append(zeroes == 4 ? "4" : "2");
            self.append('}');
        }
        self.append(Integer.toString(c, 16));
        self.append("[ \\t\\n]?|");
        if (escape) {
            self.append("\\");
        }
        self.append(o);
        self.append(")");
        return self;
    }
    
    public static Appendable appendPattern(Appendable self, Object o) throws IOException {
        if (o instanceof Character) {
            appendCharacter(self, ((Character) o));
        }
        else if (o instanceof CharSequence) {
            for (int i = 0; i < ((CharSequence) o).length(); ++i) {
                appendCharacter(self, ((CharSequence) o).charAt(i));
            }
        }
        return self;
    }
    
    public static Appendable appendPattern(Appendable self, Object... data) throws IOException {
        if (data != null) {
            for (Object o : data) {
                appendPattern(self, o);
            }
        }
        return self;
    }
    
    /**
     * Convenience method.
     * @return never <code>null</code>
     */
    public static String toPattern(Object... data) throws IOException {
        return appendPattern(new StringBuilder(), data).toString();
    }

}
