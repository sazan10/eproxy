package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*

import org.xml.sax.ContentHandler
import org.xml.sax.Locator

/**
 * SAX 2 {@link ContentHandler} that delegates to another {@link ContentHandler}.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: HTMLSerializer.java 7637 2015-08-12 10:55:33Z johann $
 */
class DelegatingContentHandler extends BaseContentHandler {

    Locator documentLocator

    @Delegate
    ContentHandler delegate

    @Override
    void setDocumentLocator(Locator documentLocator) {
        this.documentLocator = documentLocator
        delegate?.setDocumentLocator(documentLocator)
    }
    
}
