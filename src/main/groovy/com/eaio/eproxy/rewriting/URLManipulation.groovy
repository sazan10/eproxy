package com.eaio.eproxy.rewriting

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Mixin for very few URL manipulation methods. Not a <tt>trait</tt> because of weird side-effects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@Slf4j
class URLManipulation {

    /**
     * Resolves <tt>uri</tt> relative to <tt>requestURI</tt>, then turns it all into Eproxy's URL scheme.
     */
    String rewrite(URI baseURI, URI requestURI, String uri, RewriteConfig rewriteConfig = null) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI)
        URI resolvedURI = resolve(requestURI, uri)
        if (resolvedURI.scheme != 'http' && resolvedURI.scheme != 'https') {
            builder.scheme(resolvedURI.scheme + ':' + baseURI.scheme)
            resolvedURI = toURI(resolvedURI.schemeSpecificPart)
        }
        builder.pathSegment((rewriteConfig?.toString() ?: '') + resolvedURI.scheme, resolvedURI.authority)
        if (resolvedURI.rawPath) {
            builder.path(resolvedURI.rawPath)
        }
        builder.query(resolvedURI.rawQuery)
        if (resolvedURI.rawFragment) {
            builder.fragment(resolvedURI.rawFragment)
        }
        builder.build().toUriString()
    }

    /**
     * Resolves a potentially relative URI to a reference URI.
     * 
     * @return either a {@link URI} or a {@link URL}
     */
    private URI resolve(URI requestURI, String attributeValue) {
        try {
            requestURI.resolve(attributeValue)
        }
        catch (IllegalArgumentException ex) {
            if (attributeValue.contains('|')) {
                resolve(requestURI, attributeValue.replaceAll('\\|', '%7C'))
            }
            else {
                throw new IllegalArgumentException("For ${attributeValue}: ${ex.message}", ex)
            }
        }
    }
    
    URI toURI(String s) {
        try {
            s.toURI()
        }
        catch (URISyntaxException ex) {
            if (s.contains('|')) {
                s.replaceAll('\\|', '%7C').toURI() // TODO: Use ReEncoding
            }
            else {
                throw ex
            }
        }
    }

}
