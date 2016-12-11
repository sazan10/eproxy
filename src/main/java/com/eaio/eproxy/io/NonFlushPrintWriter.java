package com.eaio.eproxy.io;

import java.io.*;

/**
 * NekoHTML's {@link org.cyberneko.html.filters.Writer} class makes an awful amount of calls to {@link Writer#flush()}.
 * Those are apparently pretty expensive in Tomcat.
 * <p>
 * This class makes {@link #flush()} a no-op.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class NonFlushPrintWriter extends PrintWriter {

    /**
     * @param out
     */
    public NonFlushPrintWriter(Writer out) {
        super(out);
    }

    /**
     * No-op.
     * @see java.io.PrintWriter#flush()
     */
    @Override
    public final void flush() {}

}
