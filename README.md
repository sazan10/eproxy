[![Coverage Status](https://coveralls.io/repos/johannburkard/eproxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/johannburkard/eproxy?branch=master) [![Analytics](https://ga-beacon.appspot.com/UA-7427410-88/eproxy/README.md?pixel)](https://github.com/igrigorik/ga-beacon)

# Eproxy

Eproxy is a proxy that can also double as a cheap CDN, a proxy that

* rewrites HTML, CSS and JavaScript (planned) so you'll always stay on the proxy
* supports cookies (planned)
* supports all HTTP methods 
* supports HTTPS
* caches content
* lets you customize the socket and HTTP setup
* uses a real parser for HTML, CSS and JavaScript (planned)
* attempts to be as secure as possible 

In other words, something you can use in a company network to look at questionable sites without needing to install anything. Or a cheap accelerator for your static content.

## Features

* Caching in-memory, using Memcache or Infinispan (planned).
* Support for stand-alone operation, inside a JEE container (Tomcat or Jetty) or on Google App Engine.
* Robust, parser-based HTML and CSS rewriting.

## Demo Site

[Demo site here](https://weizentortillas.appspot.com)

### Plans

* Make JavaScript work securely by intercepting calls to ``new Image``, ``eval``, ``location`` and others.
* Get rid of VBScript because I'm sure there is one website that still uses it.
* Get rid of CSS behaviours.
 
## Security status

Not tested enough. Don't use this if you rely on security.

## Configuration

## Installation

You'll need

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) (version 7 or greater)
* [Maven](https://maven.apache.org)

### Stand-alone 

* Tomcat or Jetty

### On Google App Engine

``mvn clean package verify appengine:update``

## History

The idea is based on [mirrorr](https://github.com/bslatkin/mirrorrr). Some of the code came from [media.io](http://media.io), a project called Delivrr (sadly gone) and [eaio](http://eaio.com).

## mirrorr disadvantages

I have nothing against mirrorr but there are some problems with it:

* HTML rewriting is based on regular expressions.
* Only GET requests are supported.
* It (probably) doesn't support HTTPS.
* It only runs on Google App Engine, using Memcache as the cache.
