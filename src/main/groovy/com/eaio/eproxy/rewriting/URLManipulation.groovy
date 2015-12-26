package com.eaio.eproxy.rewriting

import org.springframework.web.util.UriComponentsBuilder

/**
 * Mixin for a few URL manipulation methods. Not a <tt>trait</tt> because of weird side-effects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class URLManipulation {
    
    String rewrite(URI baseURI, URI target, def rewriteConfig = null) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI).pathSegment((rewriteConfig?.toString() ?: '') + target.scheme, target.authority)
        if (target.rawPath) {
            builder.path(target.rawPath)
        }
        builder.query(target.rawQuery).fragment(target.rawFragment).build().toUriString()
    }
    
    /**
     * Alternative rewriting method for URIs that {@link URI} doesn't want to parse.
     */
    String rewrite(URI baseURI, URL target, def rewriteConfig = null) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI).pathSegment((rewriteConfig?.toString() ?: '') + target.protocol, target.authority)
        if (target.path) {
            builder.path(target.path)
        }
        builder.query(target.query).fragment(target.ref).build().toUriString()
    }
    
    /**
     * Resolves a potentially relative URI to a reference URI.
     * 
     * @return either a {@link URI} or a {@link URL}
     */
    Serializable resolve(URI requestURI, String attributeValue) {
        try {
            requestURI.resolve(attributeValue)
        }
        catch (IllegalArgumentException ex) {
            new URL(new URL(requestURI.scheme, requestURI.host, requestURI.port, requestURI.rawPath), attributeValue)
        }
    }
}
