package com.eaio.eproxy.rewriting.html;

import org.apache.xerces.util.AttributesProxy;
import org.apache.xerces.util.LocatorProxy;
import org.apache.xerces.xni.*;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Bridge between Xerces XNI and SAX 2.
 * <p>
 * Inspired by {@link org.apache.xerces.jaxp.JAXPValidatorComponent$XNI2SAX}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 * @see LocatorProxy
 */
public class XMLDocumentHandlerDocumentHandlerAdapter implements
        XMLDocumentHandler {
    
    private final ContentHandler contentHandler;
    
    private NamespaceContext namespaceContext;

    public XMLDocumentHandlerDocumentHandlerAdapter(
            ContentHandler contentHandler) {
        super();
        this.contentHandler = contentHandler;
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(org.apache.xerces.xni.XMLLocator, java.lang.String, org.apache.xerces.xni.NamespaceContext, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void startDocument(XMLLocator locator, String encoding,
            NamespaceContext namespaceContext, Augmentations augs)
            throws XNIException {
        this.namespaceContext = namespaceContext;
        contentHandler.setDocumentLocator(new LocatorProxy(locator));
        try {
            contentHandler.startDocument();
        }
        catch (SAXException ex) {
            throw new XNIException(ex);
        }
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#xmlDecl(java.lang.String, java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void xmlDecl(String version, String encoding, String standalone,
            Augmentations augs) throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(java.lang.String, java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void doctypeDecl(String rootElement, String publicId,
            String systemId, Augmentations augs) throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#comment(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#processingInstruction(java.lang.String, org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void processingInstruction(String target, XMLString data,
            Augmentations augs) throws XNIException {
        try {
            contentHandler.processingInstruction(target, data.toString());
        }
        catch (SAXException ex) {
            throw new XNIException(ex);
        }
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        startElement(element, attributes, augs);
        endElement(element, augs);
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#startGeneralEntity(java.lang.String, org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void startGeneralEntity(String name,
            XMLResourceIdentifier identifier, String encoding,
            Augmentations augs) throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#textDecl(java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void textDecl(String version, String encoding, Augmentations augs)
            throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#endGeneralEntity(java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void endGeneralEntity(String name, Augmentations augs)
            throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#characters(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void characters(XMLString text, Augmentations augs)
            throws XNIException {
        try {
            contentHandler.characters(text.ch, text.offset, text.length);
        }
        catch (SAXException ex) {
            throw new XNIException(ex);
        }
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#ignorableWhitespace(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
        try {
            contentHandler.ignorableWhitespace(text.ch, text.offset, text.length);
        }
        catch (SAXException ex) {
            throw new XNIException(ex);
        }
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#startCDATA(org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void startCDATA(Augmentations augs) throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#endCDATA(org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void endCDATA(Augmentations augs) throws XNIException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#endDocument(org.apache.xerces.xni.Augmentations)
     */
    @Override
    public void endDocument(Augmentations augs) throws XNIException {
        try {
            contentHandler.endDocument();
        }
        catch (SAXException ex) {
            throw new XNIException(ex);
        }
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#setDocumentSource(org.apache.xerces.xni.parser.XMLDocumentSource)
     */
    @Override
    public void setDocumentSource(XMLDocumentSource source) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#getDocumentSource()
     */
    @Override
    public XMLDocumentSource getDocumentSource() {
        throw new UnsupportedOperationException();
    }
    
    // Code stolen from JAXPValidatorComponent$XNI2SAX. Apache license.
    
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
        try {
            // start namespace prefix mappings
            int count = namespaceContext.getDeclaredPrefixCount();
            if (count > 0) {
                String prefix = null;
                String uri = null;
                for (int i = 0; i < count; i++) {
                    prefix = namespaceContext.getDeclaredPrefixAt(i);
                    uri = namespaceContext.getURI(prefix);
                    contentHandler.startPrefixMapping(prefix, (uri == null)?"":uri);
                }
            }
                    
            String uri = element.uri != null ? element.uri : "";
            String localpart = element.localpart;
            contentHandler.startElement(uri, localpart, element.rawname, new AttributesProxy(attributes));
        } catch( SAXException e ) {
            throw new XNIException(e);
        }
    }

    @Override
    public void endElement(QName element, Augmentations augs) throws XNIException {
        try {
            String uri = element.uri != null ? element.uri : "";
            String localpart = element.localpart;
            contentHandler.endElement(uri, localpart, element.rawname);
            
            // send endPrefixMapping events
            int count = namespaceContext.getDeclaredPrefixCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    contentHandler.endPrefixMapping(namespaceContext.getDeclaredPrefixAt(i));
                }
            }
        } catch( SAXException e ) {
            throw new XNIException(e);
        }
    }

}
