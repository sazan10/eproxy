package com.eaio.eproxy.api

import javax.servlet.http.HttpServletResponse

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Optionally redirects to a script (this could be Google Tag Manager for example).
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RestController
class ScriptRedirect {
    
    @Value('${script.redirect}')
    String scriptRedirect

    @RequestMapping('/script')
    void redirect(HttpServletResponse response) {
        response.setHeader('Cache-Control', 'max-age=31536000')
        if (scriptRedirect) {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY)
            response.setHeader('Location', scriptRedirect)
        }
        else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT)
        }
    }

}
