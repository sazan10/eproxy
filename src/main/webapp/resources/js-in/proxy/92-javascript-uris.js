(function() {
    
    function decodeTargetURI(uri) {
        var out = uri
        try {
            out = /(https?\/.+)/i.exec(uri)[1].replace(/^(https?)\//i, "$1://")
        }
        catch (e) {}
        return out
    }

    /**
     * Pings javascript: links back to GA.
     */
    function trackJavaScriptURIs() {
        if (([].slice.call(document.links)).filter(function(a) { return /^javascript:/i.test(this.href) }).length) {
            var currentURL = decodeTargetURI(location.href)
            eaio.track.event('TrackJavaScriptURIs', currentURL)
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
