package com.eaio.eproxy.rewriting

import groovy.transform.InheritConstructors

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@InheritConstructors
class URIAwareContentHandler extends DelegatingContentHandler {

    URI baseURI, requestURI 
    
}
