(function() {

	/**
	 * Pings the page performance back to GA.
	 */
	function trackPagePerformance() {
	    eaio.trackPagePerformance('Proxy')
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
