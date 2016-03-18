package com.eaio.eproxy.rewriting.css;

import java.io.IOException;

/**
 * Creates regular expressions that support CSS escaping.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class CSSRegExpEscaping {
    
    public static Appendable appendPattern(Appendable self, Object o) throws IOException {
        if (o instanceof Character) {
            
            self.append("(?:\\\\");
            
            int c = (int) ((Character) o).charValue();
            int zeroes = c < 256 ? 4 : c < 65536 ? 2 : 0;
            if (zeroes > 0) {
                self.append("0{0,");
                self.append(zeroes == 4 ? "4" : "2");
                self.append('}');
            }
            self.append(Integer.toString(c, 16));
            self.append("[ \\t\\n]?|");
            self.append((Character) o);
            self.append(")");
        }
        else if (o instanceof CharSequence) {
            for (int i = 0; i < ((CharSequence) o).length(); ++i) {
                appendPattern(self, ((CharSequence) o).charAt(i));
            }
        }
        return self;
    }
    
    public static String toPattern(Object... data) throws IOException {
        return appendPattern(new StringBuilder(), data).toString();
    }
    
    public static Appendable appendPattern(Appendable self, Object... data) throws IOException {
        if (data != null) {
            for (Object o : data) {
                appendPattern(self, o);
            }
        }
        return self;
    }

}
