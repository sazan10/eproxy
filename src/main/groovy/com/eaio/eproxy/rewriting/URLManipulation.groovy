package com.eaio.eproxy.rewriting

import groovy.util.logging.Slf4j

import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Trait that contains a few URL manipulation methods.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Slf4j
trait URLManipulation {

    /**
     * Resolves <tt>uri</tt> relative to <tt>requestURI</tt>, then turns it all into Eproxy's URL scheme.
     */
    String rewrite(URI baseURI, URI requestURI, String uri, RewriteConfig rewriteConfig = null) {
        URI resolvedURI = resolve(requestURI, uri)
        if (resolvedURI) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI)
            if (resolvedURI.scheme != 'http' && resolvedURI.scheme != 'https') {
                builder.scheme(resolvedURI.scheme + ':' + baseURI.scheme)
                try {
                    resolvedURI = reEncoding.reEncode(resolvedURI.schemeSpecificPart).toURI()
                }
                catch (URISyntaxException ex) {} // TODO
            }
            builder.pathSegment(rewriteConfig ? rewriteConfig.toString() + resolvedURI.scheme : resolvedURI.scheme, resolvedURI.authority)
            if (resolvedURI.rawPath) {
                builder.path(resolvedURI.rawPath)
            }
            builder.query(resolvedURI.rawQuery)
            if (resolvedURI.rawFragment) {
                builder.fragment(resolvedURI.rawFragment)
            }
            builder.build().toUriString()
        }
        else {
            baseURI
        }
    }

    /**
     * Resolves a potentially relative URI to a reference URI.
     * <p>
     * Example: <code>resolve('http://foo.com/ah/oh.html'.toURI(), '/ui.html') = 'http://foo.com/oh.html'</code>
     */
    URI resolve(URI requestURI, String attributeValue) {
        try {
            requestURI.resolve(reEncoding.reEncode(attributeValue))
        }
        catch (IllegalArgumentException ex) {
            log.warn('couldn\'t resolve {} relative to {}: {}', attributeValue, requestURI, (ExceptionUtils.getRootCause(ex) ?: ex).message)
        }
    }

}
