package com.eaio.eproxy.rewriting

import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Mixin for very few URL manipulation methods. Not a <tt>trait</tt> because of weird side-effects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URLManipulation {

    /**
     * Resolves <tt>uri</tt> relative to <tt>requestURI</tt>, then turns it all into Eproxy's URL scheme.
     */
    String rewrite(URI baseURI, URI requestURI, String uri, RewriteConfig rewriteConfig = null) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI)
        def resolvedURI = resolve(requestURI, uri)
        if (resolvedURI instanceof URI) {
            builder.pathSegment((rewriteConfig?.toString() ?: '') + resolvedURI.scheme, resolvedURI.authority)
            if (resolvedURI.rawPath) {
                builder.path(resolvedURI.rawPath)
            }
            builder.query(resolvedURI.rawQuery).fragment(resolvedURI.rawFragment)
        }
        else if (resolvedURI instanceof URL) {
            builder.pathSegment((rewriteConfig?.toString() ?: '') + resolvedURI.protocol, resolvedURI.authority)
            if (resolvedURI.path) {
                builder.path(resolvedURI.path)
            }
            builder.query(resolvedURI.query).fragment(resolvedURI.ref)
        }
        builder.build().toUriString()
    }

    /**
     * Resolves a potentially relative URI to a reference URI.
     * 
     * @return either a {@link URI} or a {@link URL}
     */
    private Serializable resolve(URI requestURI, String attributeValue) {
        try {
            requestURI.resolve(attributeValue)
        }
        catch (IllegalArgumentException ex) {
            new URL(new URL(requestURI.scheme, requestURI.host, requestURI.port, requestURI.rawPath), attributeValue)
        }
    }
    
}
