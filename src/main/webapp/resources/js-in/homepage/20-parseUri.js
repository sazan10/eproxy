//parseUri 1.2.2
//(c) Steven Levithan <stevenlevithan.com>
//MIT License

function parseUri (str) {
    var options = {
            key: ["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],
            q:   {
                name:   "queryKey",
                parser: /(?:^|&)([^&=]*)=?([^&]*)/g
            },
            parser: {
                loose:  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
            }
        };
    
    
    var m   = options.parser.loose.exec(str),
        uri = {},
        i   = 14;

    while (i--) uri[options.key[i]] = m[i] || "";

    uri[options.q.name] = {};
    uri[options.key[12]].replace(options.q.parser, function ($0, $1, $2) {
        if ($1) uri[options.q.name][$1] = $2;
    });

    return uri;
};
