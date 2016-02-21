package com.eaio.eproxy.api

import static org.apache.commons.lang3.StringUtils.*

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import com.eaio.eproxy.entities.RewriteConfig
import com.eaio.eproxy.rewriting.URIManipulation
import com.eaio.net.httpclient.ReEncoding

/**
 * Redirects users who have JavaScript disabled to {@link Proxy}.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic
@RestController
@Slf4j
class NoScriptRedirect implements URIManipulation {
    
    @Autowired
    ReEncoding reEncoding

    @RequestMapping('/redir')
    void redirect(@RequestParam String url, @RequestParam('rewriteconfig') String rewriteConfigString,
        HttpServletRequest request, HttpServletResponse response) {
        URI baseURI = buildBaseURI(request.scheme, request.serverName, request.serverPort, request.contextPath)
        
        String reEncodedURL = reEncoding.reEncode(trimToEmpty(url))
        URI resolvedURI = URI.create(reEncodedURL)
        if (resolvedURI.scheme) {
            resolvedURI = new URI(resolvedURI.scheme, resolvedURI.host, null, null)
        }
        else {
            resolvedURI = new URI(request.scheme, substringBefore(resolvedURI.rawPath, '/'), null, null)
            reEncodedURL = substringAfter(reEncodedURL, '/')
        }
        
        String targetURI = encodeTargetURI(baseURI, resolvedURI, reEncodedURL, RewriteConfig.fromString(rewriteConfigString))
        response.sendRedirect(targetURI)
    }

}
