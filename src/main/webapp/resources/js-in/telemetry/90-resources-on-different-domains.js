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
	 * Scans the document (via the Resource Timing API
	 * https://www.w3.org/TR/resource-timing/) for resources on another domain,
	 * i.e. things that eproxy didn't rewrite correctly.
	 */
	function scanDocumentForResourcesOnDifferentDomains() {
		var resourceEntries = window.performance.getEntriesByType('resource'),
			i, r0, currentURL = decodeTargetURI(location.href)
		for (i = 0; i < resourceEntries.length; ++i) {
			r0 = resourceEntries[i]
			if (!/^data:/i.test(r0.name) && !r0.name.startsWith(location.origin) && !r0.name.startsWith('https://www.google-analytics.com/collect')) {
				eaio.track.event('ResourceOnDifferentDomains', currentURL, decodeTargetURI(r0.name))
			}
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
