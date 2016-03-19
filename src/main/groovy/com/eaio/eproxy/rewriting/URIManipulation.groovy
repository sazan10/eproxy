package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig

/**
 * Trait that contains a few URL manipulation methods.
 * <p>
 * Accesses
 * <ul>
 * <li><tt>{@link ReEncoding reEncoding}</tt></li>
 * <li><tt>log</tt></li>
 * </ul>
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
trait URIManipulation {

    /**
     * Builds a URI that points to this web application.
     */
    @CompileStatic
    URI buildBaseURI(String scheme, String host, int port, String contextPath) {
        new URI(scheme, null, host, getPort(scheme, port), contextPath, null, null)
    }

    /**
     * Decodes a URI from eproxy's scheme.
     * <p> 
     * Make sure to remove the context path before calling this method.
     */
    URI decodeTargetURI(String scheme, String requestURI, String queryString) {
        String uriFromHost = substringAfter(requestURI[1..-1], '/'), path = substringAfter(uriFromHost, '/') ?: '/',
            hostAndPort = substringBefore(uriFromHost, '/'), host = hostAndPort, port
        if (hostAndPort.contains(':')) {
            host = substringBefore(hostAndPort, ':')
            port = substringAfter(hostAndPort, ':')
        }
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme(scheme).host(host).path(path)
        if (port) {
            builder.port(port)
        }
        if (queryString) {
            reEncoding.reEncode(builder.build().toUriString() + '?' + queryString).toURI()
        }
        else {
            reEncoding.reEncode(builder.build() as String).toURI()
        }
    }

    /**    
     * Returns <tt>-1</tt> if <tt>port</tt> is 80 (for the "HTTP" scheme) or 443 (for the "HTTPS" scheme).
     */
    @CompileStatic
    int getPort(String scheme, int port) {
        (scheme?.equalsIgnoreCase('http') && port == 80I) || (scheme?.equalsIgnoreCase('https') && port == 443I) ? -1I : port
    }

    /**
     * Resolves <tt>uri</tt> relative to <tt>requestURI</tt>, then turns it all into eproxy's scheme.
     */
    String encodeTargetURI(URI baseURI, URI requestURI, String uri, RewriteConfig rewriteConfig = null) {
        URI resolvedURI = resolve(requestURI, uri)
        if (resolvedURI) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseURI)
            if (!(resolvedURI.scheme?.equalsIgnoreCase('http')) && !(resolvedURI.scheme?.equalsIgnoreCase('https'))) {
                builder.scheme(resolvedURI.scheme + ':' + baseURI.scheme)
                try {
                    resolvedURI = reEncoding.reEncode(resolvedURI.schemeSpecificPart).toURI()
                }
                catch (URISyntaxException ex) {
                    log.warn('couldn\'t resolve {}: {}', resolvedURI.schemeSpecificPart, (ExceptionUtils.getRootCause(ex) ?: ex).message)
                }
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
            uri
        }
    }

    /**
     * Resolves a potentially relative URI to a reference URI.
     * Also "repairs" incomplete URL encoding by using the "healthy" prefix.
     * <p>
     * Example:
     * <code>resolve('http://foo.com/ah/oh.html'.toURI(), '/ui.html') = 'http://foo.com/ui.html'</code>
     */
    URI resolve(URI requestURI, String attributeValue) {
        String reEncodedAttributeValue = reEncoding.reEncode(attributeValue)
        try {
            requestURI.resolve(reEncodedAttributeValue)
        }
        catch (IllegalArgumentException ex) {
            if (ex.message?.startsWith('Malformed escape pair at index')) {
                int index = (ex.message =~ /Malformed escape pair at index (\d+)/)[0I][1I] as int
                requestURI.resolve(reEncodedAttributeValue.substring(0I, index))
            }
            else {
                log.warn('couldn\'t resolve {} relative to {}: {}', abbreviate(attributeValue, 100I),
                    requestURI, (ExceptionUtils.getRootCause(ex) ?: ex).message)
            }
        }
    }

}
