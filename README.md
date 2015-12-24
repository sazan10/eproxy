[![Coverage Status](https://coveralls.io/repos/johannburkard/eproxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/johannburkard/eproxy?branch=master)

# Eproxy

Even since coming across [mirrorr](https://github.com/bslatkin/mirrorrr) I've wanted to have a content-rewriting proxy that could also double as a cheap CDN, a proxy that

* rewrites HTML, CSS and JavaScript so you'll always stay on the proxy
* supports cookies
* supports all HTTP methods 
* supports HTTPS
* caches content
* lets you customize the socket and HTTP setup
* uses a real parser for HTML, CSS and JavaScript
* attempts to be as secure as possible 

In other words, something you can use in a company network to look at questionable sites without needing to install anything. Or a cheap accelerator for your static content.

## mirrorr disadvantages

I have nothing against mirrorr but there are some problems with it:

* HTML rewriting is based on regular expressions.
* Only GET requests are supported.
* It (probably) doesn't support HTTPS.
* It only runs on Google App Engine, using Memcache as the cache.

## Security status

Totally untested. Don't use this.

## Installation

* Java 7 or greater
* Maven
* Tomcat or Jetty

### On Google App Engine

* App Engine SDK
