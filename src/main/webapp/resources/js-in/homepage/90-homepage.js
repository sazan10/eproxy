document.getElementById('submit').onclick = function() {
    var url = document.getElementById('url').value.replace(/^\s\s*/, '').replace(/\s\s*$/, '')
    if (url) {
        var uri = parseUri(url), encodedHost = punycode.ToASCII(uri['host'])
        uri['authority'] = uri['authority'].replace(uri.host, encodedHost)
        uri['host'] = encodedHost
        try {
            ga('send', 'pageview', '/proxy')
        }
        catch (e) {}
        var encodedURI = (uri['protocol'] || 'http') + '/' + uri['authority'] + (uri['relative'] ? uri['relative'] : '/')
        location.href = parseUri(location.href)['directory'] + 'rwn-' + encodedURI
    }
    return false
}

document.getElementById('url').onkeydown = function() {
    try {
        ga('send', 'event', 'URL', 'keydown')
    }
    catch (e) {}
    this.onkeydown = function() {}
}
