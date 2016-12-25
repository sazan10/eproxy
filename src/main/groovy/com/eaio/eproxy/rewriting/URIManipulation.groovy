package com.eaio.eproxy.rewriting

import static org.apache.commons.lang3.StringUtils.*
import groovy.transform.CompileStatic

import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriComponentsBuilder

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.net.httpclient.ReEncoding

/**
 * Trait that contains a few URL manipulation methods.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
trait URIManipulation {
    
    Logger log = LoggerFactory.getLogger(getClass())

    /**
     * Builds a URI that points to this web application.
     */
    URI buildBaseURI(String scheme, String host, int port, String contextPath) {
        new URI(scheme, null, host, getPort(scheme, port), contextPath, null, null)
    }

    /**
     * Decodes a URI from eproxy's scheme.
     * <p> 
     * Make sure to remove the context path before calling this method.
     */
    URI decodeTargetURI(String scheme, String requestURI, String queryString) {
        String uriFromHost = substringAfter(requestURI[1..-1], '/'),
            hostAndPort = substringBefore(uriFromHost, '/'),
            path = substringAfter(uriFromHost, hostAndPort),
            host = hostAndPort,
            port
        if (hostAndPort.contains(':')) {
            host = substringBefore(hostAndPort, ':')
            port = substringAfter(hostAndPort, ':')
        }
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
            .scheme(scheme)
            .host(host?.toLowerCase())
            .path(path)
        if (port) {
            if (!port.isInteger()) {
                throw new NumberFormatException("Not a number: ${port}")
            }
            builder.port(port)
        }
        if (queryString) {
            ReEncoding.INSTANCE.reEncode(builder.build().toString() + '?' + queryString).toURI()
        }
        else {
            ReEncoding.INSTANCE.reEncode(builder.build().toString()).toURI()
        }
    }

    /**    
     * Returns <tt>-1</tt> if <tt>port</tt> is 80 (for the "HTTP" scheme) or 443 (for the "HTTPS" scheme).
     */
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
                    resolvedURI = ReEncoding.INSTANCE.reEncode(resolvedURI.schemeSpecificPart).toURI()
                }
                catch (URISyntaxException ex) {
                    log.warn('couldn\'t resolve {}: {}', resolvedURI.schemeSpecificPart, (ExceptionUtils.getRootCause(ex) ?: ex).message)
                }
            }
            builder
                .pathSegment(rewriteConfig.asBoolean() ? rewriteConfig.toString() + resolvedURI.scheme : resolvedURI.scheme, resolvedURI.authority)
                .path(resolvedURI.rawPath ?: '/')
                .query(resolvedURI.rawQuery)
            if (resolvedURI.rawFragment) {
                builder.fragment(resolvedURI.rawFragment)
            }
            builder.build().toString()
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
        String reEncodedAttributeValue = ReEncoding.INSTANCE.reEncode(attributeValue)
        try {
            requestURI.resolve(reEncodedAttributeValue)
        }
        catch (IllegalArgumentException ex) {
            // Try to "Repair" malformed URLs, see URISyntaxException#getMessage()
            if (ex.message?.contains(' at index ')) {
                int index = substringAfterLast(substringBefore(ex.message, ':'), ' ') as int
                try {
                    requestURI.resolve(reEncodedAttributeValue.substring(0I, index))
                }
                catch (IllegalArgumentException ignored) {}
            }
            else {
                log.warn('couldn\'t resolve {} relative to {}: {}', abbreviate(attributeValue, 100I),
                    requestURI, (ExceptionUtils.getRootCause(ex) ?: ex).message)
            }
        }
    }
    
    /**
     * Removes the context path prefix from <tt>requestURI</tt>.
     */
    String stripContextPathFromRequestURI(String contextPath, String requestURI) {
        substringAfter(requestURI, contextPath)
    }

}
