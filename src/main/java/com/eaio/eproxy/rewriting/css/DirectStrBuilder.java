package com.eaio.eproxy.rewriting.css;

import org.apache.commons.lang3.text.StrBuilder;

/**
 * A {@link StrBuilder} with direct access to the char array, for memory reasons.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class DirectStrBuilder extends StrBuilder {
    
    private static final long serialVersionUID = -6168436101601496505L;
    
    public DirectStrBuilder(int initialCapacity) {
        super(initialCapacity);
    }

    public char[] getBuffer() {
        return buffer;
    }

}
