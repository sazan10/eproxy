package com.eaio.eproxy.rewriting.css;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

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

    private static final String[][] CSS_UNESCAPE = new String[][] {
        { "&lpar;", "(" },
        { "&rpar;", ")" }
    };

    public static CharSequenceTranslator UNESCAPE_CSS = new LookupTranslator(CSS_UNESCAPE);

    public static CharSequence unescapeCSS(CharSequence input) {
        return UNESCAPE_CSS.translate(input);
    }

    public static Collection<Pattern> PATTERNS;

    private static Pattern patternURLImage,
        patternImport,
        patternSrcColorSpace;

    //  Pattern.compile("(?:url|image)\\s*\\(\\s*([\"']([^#][^\"']+)[\"']|([^#][^\\s)]+))", Pattern.CASE_INSENSITIVE),
    //  Pattern.compile("(?:url|image)\\s*\\(\\s*([\"']([^\"']+)[\"']|([^\\s)]+))", Pattern.CASE_INSENSITIVE),
    static {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "url");
            builder.append('|');
            CSSEscapeRegEx.appendPattern(builder, "image");
            builder.append(")\\s*");
            CSSEscapeRegEx.appendCharacter(builder, '(', true);
            builder.append("\\s*(");

            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "\"");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "'");
            builder.append(")");

            builder.append("([^\"']+)");

            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "\"");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "'");
            builder.append(")|([^\\s)]+))");

            patternURLImage = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    //  Pattern.compile("@import\\s*(?:[\"']([^#][^\"']+)[\"']|([^#][^\\s;]+))", Pattern.CASE_INSENSITIVE),
    //  Pattern.compile("@import\\s*(?:[\"']([^\"']+)[\"']|([^\\s;]+))", Pattern.CASE_INSENSITIVE),
    static {
        try {
            StringBuilder builder = new StringBuilder();
            CSSEscapeRegEx.appendPattern(builder, "@import");
            builder.append("\\s(?:");

            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "\"");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "'");
            builder.append(")");

            builder.append("([^\"']+)");

            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "\"");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "'");
            builder.append(")|([^\\s;]+))");

            patternImport = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    //  Pattern.compile("\\W(?:src|colorSpace)\\s*=\\s*(?:[\"']([^#][^\"']+)[\"']|([^#][^\\s)]+))", Pattern.CASE_INSENSITIVE)),
    //  Pattern.compile("\\W(?:src|colorSpace)\\s*=\\s*(?:[\"']([^\"']+)[\"']|([^\\s)]+))", Pattern.CASE_INSENSITIVE))
    static {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("\\W(?:");
            CSSEscapeRegEx.appendPattern(builder, "src");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "colorSpace");
            builder.append(")\\s*=\\s*(?:");

            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "\"");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "'");
            builder.append(")");

            builder.append("([^\"']+)");

            builder.append("(?:");
            CSSEscapeRegEx.appendPattern(builder, "\"");
            builder.append("|");
            CSSEscapeRegEx.appendPattern(builder, "'");
            builder.append(")");

            builder.append("|([^\\s)]+))");

            patternSrcColorSpace = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static {
        PATTERNS = Collections.unmodifiableList(Arrays.asList(patternURLImage,
                patternImport,
                patternSrcColorSpace));
    }

}
