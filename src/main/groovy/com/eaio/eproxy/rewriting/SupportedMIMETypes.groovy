package com.eaio.eproxy.rewriting

import org.springframework.stereotype.Component

/**
 * MIME types that can be rewritten.
 * 
 * @author <a href='mailto:johann@johannburkard.de'>Johann Burkard</a>
 * @version $Id$
 */
@Component
class SupportedMIMETypes {

    Set<String> javascript = [
        'application/ecmascript',
        'application/javascript',
        'application/x-ecmascript',
        'application/x-javascript',
        'text/ecmascript',
        'text/javascript',
        'text/javascript1.0',
        'text/javascript1.1',
        'text/javascript1.2',
        'text/javascript1.3',
        'text/javascript1.4',
        'text/javascript1.5',
        'text/jscript',
        'text/livescript',
        'text/x-ecmascript',
        'text/x-javascript',
    ] as Set

    // TODO: Get rid of VBScript

    private Set<String> html = [
        'text/html',
        'text/x-server-parsed-html',
        'application/xml+xhtml',
    ] as Set

    boolean isHTML(String mimeType) {
        html.contains(mimeType?.toLowerCase() ?: '')
    }

    /*private Set<String> css = [
        'text/css',
    ] as Set*/

    boolean isCSS(String mimeType) {
        mimeType?.equalsIgnoreCase('text/css')
    }

}
