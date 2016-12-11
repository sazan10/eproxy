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
     * @param out
     */
    public NonFlushPrintWriter(OutputStream out) {
        super(out);
    }

    /**
     * @param fileName
     * @throws FileNotFoundException
     */
    public NonFlushPrintWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * @param file
     * @throws FileNotFoundException
     */
    public NonFlushPrintWriter(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * @param out
     * @param autoFlush
     */
    public NonFlushPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * @param out
     * @param autoFlush
     */
    public NonFlushPrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * @param fileName
     * @param csn
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public NonFlushPrintWriter(String fileName, String csn)
                    throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    /**
     * @param file
     * @param csn
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public NonFlushPrintWriter(File file, String csn)
                    throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    /**
     * No-op.
     * @see java.io.PrintWriter#flush()
     */
    @Override
    public final void flush() {}

}
