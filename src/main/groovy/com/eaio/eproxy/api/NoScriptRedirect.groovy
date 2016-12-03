package com.eaio.eproxy.api

import static org.apache.commons.lang3.StringUtils.*

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.URIManipulation
import com.eaio.net.httpclient.ReEncoding

/**
 * Redirects users who have JavaScript disabled to {@link com.eaio.eproxy.api.Proxy}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@RestController
@Slf4j
class NoScriptRedirect implements URIManipulation {

    @RequestMapping('/redir')
    void redirect(@RequestParam String url, @RequestParam('rewriteconfig') String rewriteConfigString,
            HttpServletRequest request, HttpServletResponse response) {
        URI baseURI = buildBaseURI(request.scheme, request.serverName, request.serverPort, request.contextPath)
        String targetURI

        if (url) {
            String reEncodedURL
            URI resolvedURI = URI.create(trimToEmpty(url))
            if (resolvedURI.scheme) {
                reEncodedURL = ReEncoding.INSTANCE.reEncode(trimToEmpty(substringAfter(url, resolvedURI.rawAuthority)))
                        resolvedURI = new URI(resolvedURI.scheme, IDN.toASCII(resolvedURI.rawAuthority), null, null)
            }
            else {
                reEncodedURL = ReEncoding.INSTANCE.reEncode(trimToEmpty(substringAfter(url, '/')))
                        resolvedURI = new URI(request.scheme, IDN.toASCII(substringBefore(resolvedURI.rawPath, '/')), null, null)
            }
            
            targetURI = encodeTargetURI(baseURI, resolvedURI, reEncodedURL, RewriteConfig.fromString(rewriteConfigString))
        }
        
        response.setHeader('Cache-Control', 'max-age=31536000')
        response.sendRedirect(targetURI ?: baseURI.toString())
    }

}
