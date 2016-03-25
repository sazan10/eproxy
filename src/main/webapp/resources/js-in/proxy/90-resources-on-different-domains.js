(function() {

    function decodeTargetURI(uri) {
        return /(https?\/.+)/i.exec(uri)[1].replace(/^(https?)\//i, "$1://")
    }

    /**
     * Scans the document (via the Resource Timing API
     * https://www.w3.org/TR/resource-timing/) for resources on another domain,
     * i.e. things that eproxy didn't rewrite correctly.
     */
    function scanDocumentForResourcesOnDifferentDomains() {
        try {
            if (window.performance && window.performance.getEntriesByType) {
                var resourceEntries = window.performance.getEntriesByType('resource'),
                    i, r0, currentURL = decodeTargetURI(location.href)
                for (i = 0; i < resourceEntries.length; ++i) {
                    r0 = resourceEntries[i]
                    if (!/^data:/i.test(r0.name) && !/https?:\/\/(localhost|127.0.0.1)(:\d+)?\//i.test(r0.name) && !new RegExp("^" + location.origin).test(r0.name) && !r0.name.startsWith('https://www.google-analytics.com/collect')) {
                        eaio.track.event('resources-on-different-domains', currentURL, decodeTargetURI(r0.name))
                        break
                    }
                }
            }
        }
        catch (e) {
            eaio.track.exception('resources-on-different-domains', e)
        }
    }

    if (/m/.test(document.readyState)) { // coMplete
        scanDocumentForResourcesOnDifferentDomains()
    } else {
        if ("undefined" != typeof window.attachEvent) {
            window.attachEvent("onload", scanDocumentForResourcesOnDifferentDomains)
        } else if (window.addEventListener) {
            window.addEventListener("load", scanDocumentForResourcesOnDifferentDomains, false)
        }
    }

})()
