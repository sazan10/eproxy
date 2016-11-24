(function() {

    function decodeTargetURI(uri) {
        return /(https?\/.+)/i.exec(uri)[1].replace(/^(https?)\//i, "$1://")
    }

    /**
     * Pings data: URIs that aren't images back to GA.
     */
    function trackDataURIs() {
        try {
            if (window.performance && window.performance.getEntriesByType) {
                var resourceEntries = window.performance.getEntriesByType('resource'),
                    i, r0, currentURL = decodeTargetURI(location.href)
                for (i = 0; i < resourceEntries.length; ++i) {
                    r0 = resourceEntries[i]
                    if (/data:/i.test(r0.name) && /data:(text\/html|text\/x-server-parsed-html|application\/xml+xhtml|image\/svg+xml)'/i.test(r0.name)) {
                        eaio.track.event('data-uris', currentURL, r0.name)
                        break
                    }
                }
            }
        }
        catch (e) {
            eaio.track.exception('javascript-uris', e)
        }
    }

    if (/m/.test(document.readyState)) { // coMplete
        trackDataURIs()
    } else {
        if ("undefined" != typeof window.attachEvent) {
            window.attachEvent("onload", trackDataURIs)
        } else if (window.addEventListener) {
            window.addEventListener("load", trackDataURIs, false)
        }
    }

})()
