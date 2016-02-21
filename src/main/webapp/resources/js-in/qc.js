// /(https?\/.+)/.exec(location.href)[1].replace(/^(https?)\//i, "$1://")

var resourceEntries = window.performance.getEntriesByType('resource'), i, r0
for (i = 0; i < resourceEntries.length; ++i) {
  r0 = resourceEntries[i]
  window.console && console.info(r0.name.startsWith(location.origin))
}
