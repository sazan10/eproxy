package com.eaio.eproxy.rewriting.css;

import java.io.IOException;

/**
 * Creates regular expressions that support CSS escaping.
 * <p>
 * For example a character <tt>é</tt> is turned into <tt>(?:\\\\0{0,4}e9[ \\t\\n]?|é)</tt>.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class CSSEscapeRegEx {
    
    /**
     * For use with Groovy categories.
     * 
     * @return never <code>null</code>
     */
    public static Appendable appendRegEx(Appendable self, Character o) throws IOException {
        return appendRegEx(self, o, false);
    }
    
    /**
     * For use with Groovy categories.
     * 
     * @param escape whether to escape the character
     * @return never <code>null</code>
     */
    public static Appendable appendRegEx(Appendable self, Character o, boolean escape) throws IOException {
        if (self == null) {
            self = new StringBuilder();
        }
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
    
    /**
     * For use with Groovy categories.
     * 
     * @return never <code>null</code>
     */
    public static Appendable appendRegEx(Appendable self, Object o) throws IOException {
        return appendRegEx(self, o, false);
    }
    
    /**
     * For use with Groovy categories.
     * 
     * @return never <code>null</code>
     */
    public static Appendable appendRegEx(Appendable self, Object o, boolean escape) throws IOException {
        if (self == null) {
            self = new StringBuilder();
        }
        if (o instanceof Character) {
            appendRegEx(self, ((Character) o), escape);
        }
        else if (o instanceof CharSequence) {
            for (int i = 0; i < ((CharSequence) o).length(); ++i) {
                appendRegEx(self, ((CharSequence) o).charAt(i), escape);
            }
        }
        return self;
    }
    
    /**
     * For use with Groovy categories.
     * 
     * @return never <code>null</code>
     */
    public static Appendable appendRegEx(Appendable self, Object... data) throws IOException {
        if (self == null) {
            self = new StringBuilder();
        }
        if (data != null) {
            for (Object o : data) {
                appendRegEx(self, o);
            }
        }
        return self;
    }
    
    /**
     * Convenience method.
     * 
     * @return never <code>null</code>
     */
    public static String toPattern(Object... data) throws IOException {
        return appendRegEx(null, data).toString();
    }

}
