package com.eaio.eproxy.rewriting.html;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xml.serialize.BaseMarkupSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX 2 {@link ContentHandler} that outputs something vaguely resembling HTML.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 * @see BaseMarkupSerializer
 * @see org.apache.xml.serialize.HTMLSerializer
 */
public class HTMLSerializer extends BaseContentHandler {

    private final Set<String> emptyElements = new TreeSet<String>(Arrays.asList("area", "base", "basefont", "br", "col", "frame", "hr", "img", "input", "isindex", "link", "meta", "param"));

    private Locator locator;

    private final Writer output;

    public HTMLSerializer(Writer output) {
        this.output = output;
    }

    public Locator getDocumentLocator() {
        return locator;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        getStack().clear();
    }

    @Override
    public void endDocument() throws SAXException {
        getStack().clear();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        try {
            output.write("<!doctype ");
            output.write(prefix);
            output.write(">\n");
        }
        catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if (!emptyElements.contains(name(localName, qName))) {
                getStack().push(name(localName, qName));
            }
            output.write('<');
            output.write(name(localName, qName));
            for (int i = 0; i < atts.getLength(); ++i) {
                output.write(' ');
                output.write(name(atts.getLocalName(i), atts.getQName(i)));
                if (isNotBlank(atts.getValue(i))) {
                    output.write("=\"");
                    output.write(escapeHtml4(atts.getValue(i)));
                    output.write("\"");
                }
            }
            output.write('>');
        }
        catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!emptyElements.contains(name(localName, qName))) {
            try {
                getStack().pop();
            }
            catch (EmptyStackException ex) {}
            try {
                output.write("</");
                output.write(name(localName, qName));
                output.write(">");
            }
            catch (IOException ex) {
                throw new SAXException(ex);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            output.write(ch, start, length);
        }
        catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            output.write(ch, start, length);
        }
        catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

}
