package com.eaio.eproxy.rewriting;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
public class DefaultHandlerContentHandlerAdapter extends DefaultHandler {

    private final ContentHandler contentHandler;
    
    public DefaultHandlerContentHandlerAdapter(ContentHandler contentHandler) {
        super();
        this.contentHandler = contentHandler;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        if (contentHandler instanceof EntityResolver) {
            return ((EntityResolver) contentHandler).resolveEntity(publicId, systemId);
        }
        else {
            return null;
        }
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
        if (contentHandler instanceof DTDHandler) {
            ((DTDHandler) contentHandler).notationDecl(name, publicId, systemId);
        }
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
        if (contentHandler instanceof DTDHandler) {
            ((DTDHandler) contentHandler).unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        contentHandler.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        contentHandler.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        contentHandler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        contentHandler.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        contentHandler.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        contentHandler.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        contentHandler.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        contentHandler.processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        contentHandler.skippedEntity(name);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        if (contentHandler instanceof ErrorHandler) {
            ((ErrorHandler) contentHandler).warning(e);
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        if (contentHandler instanceof ErrorHandler) {
            ((ErrorHandler) contentHandler).error(e);
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        if (contentHandler instanceof ErrorHandler) {
            ((ErrorHandler) contentHandler).fatalError(e);
        }
    }

}
