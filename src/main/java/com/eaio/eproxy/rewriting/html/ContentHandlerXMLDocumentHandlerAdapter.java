package com.eaio.eproxy.rewriting.html;

import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SAXLocatorWrapper;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.xni.*;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Bridge between SAX 2 and Xerces XNI.
 * <p>
 * Inspired by {@link org.apache.xerces.jaxp.JAXPValidatorComponent$SAX2XNI}
 * and {@link com.sun.org.apache.xerces.internal.util.SAX2XNI}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@SuppressWarnings("restriction")
public class ContentHandlerXMLDocumentHandlerAdapter extends DefaultHandler {
    
    private final XMLDocumentHandler xmlDocumentHandler;
    
    private final NamespaceSupport namespaceSupport = new NamespaceSupport();
    
    private final SymbolTable symbolTable = new SymbolTable(); // TODO: There's a soft ref version, too.
    
    private XMLLocator locator = new SimpleLocator(null, null, -1, -1);

    public ContentHandlerXMLDocumentHandlerAdapter(
            XMLDocumentHandler xmlDocumentHandler) {
        super();
        this.xmlDocumentHandler = xmlDocumentHandler;
    }
    
    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = new SAXLocatorWrapper();
        ((SAXLocatorWrapper) this.locator).setLocator(locator);
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        try {
            namespaceSupport.reset();
            xmlDocumentHandler.startDocument(locator, null, namespaceSupport, null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        try {
            xmlDocumentHandler.endDocument(null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        namespaceSupport.pushContext();
        namespaceSupport.declarePrefix(prefix, uri);
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        namespaceSupport.popContext();
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        try {
            xmlDocumentHandler.startElement(toQName(uri, localName, qName), toXMLAttributes(atts), null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        try {
            xmlDocumentHandler.endElement(toQName(uri, localName, qName), null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            xmlDocumentHandler.characters(new XMLString(ch, start, length), null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        try {
            xmlDocumentHandler.ignorableWhitespace(new XMLString(ch, start, length), null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        try {
            xmlDocumentHandler.processingInstruction(symbolize(target), new XMLString(data.toCharArray(), 0, data.length()), null);
        }
        catch (XNIException ex) {
            throw toSAXException(ex);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
    }
    
    private XMLAttributes toXMLAttributes(Attributes atts) {
        XMLAttributesImpl out = new XMLAttributesImpl(atts.getLength());
        for (int i = 0; i < atts.getLength(); ++i) {
            out.addAttribute(toQName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i)), atts.getType(i), atts.getValue(i));
        }
        return out;
    }

    // Code stolen from JAXPValidatorComponent$SAX2XNI. Apache license.
    
    /**
     * Converts the {@link XNIException} received from a downstream
     * component to a {@link SAXException}.
     */
    private SAXException toSAXException( XNIException xe ) {
        Exception e = xe.getException();
        if( e==null )   e = xe;
        if( e instanceof SAXException )  return (SAXException)e;
        return new SAXException(e);
    }
    
    /**
     * Creates a proper {@link QName} object from 3 parts.
     * <p>
     * This method does the symbolization.
     */
    private QName toQName( String uri, String localName, String qname ) {
        String prefix = null;
        int idx = qname.indexOf(':');
        if( idx>0 )
            prefix = symbolize(qname.substring(0,idx));

        localName = symbolize(localName);
        qname = symbolize(qname);
        uri = symbolize(uri);

        return new QName(prefix, localName, qname, uri);
    }

    private String symbolize( String s ) {
        return symbolTable.addSymbol(s);
    }

}
