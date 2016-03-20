package com.eaio.eproxy.rewriting.html

import groovy.transform.CompileStatic

import org.apache.commons.lang3.StringEscapeUtils
import org.apache.xerces.util.XMLAttributesImpl
import org.apache.xerces.xni.Augmentations
import org.apache.xerces.xni.QName
import org.apache.xerces.xni.XMLAttributes
import org.apache.xerces.xni.XMLString
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Embeds the telemetry JavaScript on the page (if enabled).
 * <p>
 * Needs to be placed after {@link RemoveActiveContentFilter} obviously.
 * <p>
 * This class is a also good example of why every XML/SGML API is shit.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@Component
@ConditionalOnProperty(name = 'telemetry.enabled')
@CompileStatic
class TelemetryFilter extends BaseFilter {
    
    @Value('${telemetry.trackingID}')
    String trackingID

    @Value('${telemetry.hostName}')
    String hostName
    
    @Lazy
    private QName scriptElement = new QName(null, 'script', 'script', null),
        asyncAttribute = new QName(null, 'async', 'async', null),
        srcAttribute = new QName(null, 'src', 'src', null)
    
    @Lazy
    private XMLString trackingIDXMLString = {
        String trackingIDConfig = "(eproxy = window.eproxy || {}).trackingID = \"${StringEscapeUtils.escapeEcmaScript(trackingID)}\""
        new XMLString(trackingIDConfig.toCharArray(), 0I, trackingIDConfig.length())
    }()
    
    @Lazy
    private XMLString hostNameConfigXMLString = {
        String hostNameConfig = "(eproxy = window.eproxy || {}).hostName = \"${StringEscapeUtils.escapeEcmaScript(hostName)}\""
        new XMLString(hostNameConfig.toCharArray(), 0I, hostNameConfig.length())
    }()
    
    @Override
    void endElement(QName element, Augmentations augs) {
        if (nameIs(element, 'body')) {
            writeJavaScript(trackingIDXMLString)
            if (hostName) {
                writeJavaScript(hostNameConfigXMLString)
            }
            writeTelemetryJavaScript()
        }
        super.endElement(element, augs)
    }
    
    private void writeJavaScript(XMLString xmlString) {
        super.startElement(scriptElement, null, null)
        super.characters(xmlString, null)
        super.endElement(scriptElement, null)
    }
    
    private writeTelemetryJavaScript() {
        XMLAttributes atts = new XMLAttributesImpl(2I)
        atts.addAttribute(asyncAttribute, null, 'async')
        atts.addAttribute(srcAttribute, null, (((ServletRequestAttributes) RequestContextHolder.requestAttributes)?.request?.contextPath ?: '') + '/resources/js/telemetry.js')
        super.startElement(scriptElement, atts, null)
        super.endElement(scriptElement, null)
    }

}
