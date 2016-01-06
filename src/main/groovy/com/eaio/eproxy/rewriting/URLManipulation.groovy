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
        def resolved = resolve(requestURI, uri)
        if (resolved instanceof URI) {
            URI resolvedURI = (URI) resolved
            if (resolvedURI.scheme != 'http' && resolvedURI.scheme != 'https') {
                builder.scheme(resolvedURI.scheme + ':' + baseURI.scheme)
                resolvedURI = resolvedURI.schemeSpecificPart.toURI()
            }
            builder.pathSegment((rewriteConfig?.toString() ?: '') + resolvedURI.scheme, resolvedURI.authority)
            if (resolvedURI.rawPath) {
                builder.path(resolvedURI.rawPath)
            }
            builder.query(resolvedURI.rawQuery).fragment(resolvedURI.rawFragment)
        }
        else if (resolved instanceof URL) {
            URL resolvedURL = (URL) resolved
            // fallback for view-source URIs above doesn't seem to work with java.net.URL
            builder.pathSegment((rewriteConfig?.toString() ?: '') + resolvedURL.protocol, resolvedURL.authority)
            if (resolvedURL.path) {
                builder.path(resolvedURL.path)
            }
            builder.query(resolvedURL.query).fragment(resolvedURL.ref)
        }
        // TODO: Decide what to do is resolved == null
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
            try {
                new URL(new URL(requestURI.scheme, requestURI.host, requestURI.port, requestURI.rawPath), attributeValue)
            }
            catch (MalformedURLException ex2) {
                log.info('Couldn\'t convert {} to a URI or a URL: {}', attributeValue, ExceptionUtils.getRootCauseMessage(ex2))
            }
        }
    }
    
}
