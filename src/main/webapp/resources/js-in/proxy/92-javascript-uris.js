(function() {

    function decodeTargetURI(uri) {
        return /(https?\/.+)/i.exec(uri)[1].replace(/^(https?)\//i, "$1://")
    }

    /**
     * Pings javascript: links back to GA.
     */
    function trackJavaScriptURIs() {
        try {
            if (([].slice.call(document.links)).filter(function(a) { return /^javascript:/i.test(this.href) }).length) {
                var currentURL = decodeTargetURI(location.href)
                eaio.track.event('TrackJavaScriptURIs', currentURL)
            }
        }
        catch (e) {
            eaio.track.exception('javascript-uris', e)
        }
    }

    if (/m/.test(document.readyState)) { // coMplete
        trackJavaScriptURIs()
    } else {
        if ("undefined" != typeof window.attachEvent) {
            window.attachEvent("onload", trackJavaScriptURIs)
        } else if (window.addEventListener) {
            window.addEventListener("load", trackJavaScriptURIs, false)
        }
    }

})()
