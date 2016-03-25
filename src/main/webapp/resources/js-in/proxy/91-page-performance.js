(function() {

	/**
	 * Pings the page performance back to GA.
	 */
	function trackPagePerformance() {
	    eaio.track.pagePerformance('Proxy')
	}

	if (/m/.test(document.readyState)) { // coMplete
		trackPagePerformance()
	} else {
		if ("undefined" != typeof window.attachEvent) {
			window.attachEvent("onload", trackPagePerformance)
		} else if (window.addEventListener) {
			window.addEventListener("load", trackPagePerformance, false)
		}
	}

})()
