[![Coverage Status](https://coveralls.io/repos/johannburkard/eproxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/johannburkard/eproxy?branch=master) [![Analytics](https://ga-beacon.appspot.com/UA-7427410-88/eproxy/README.md?pixel)](https://github.com/igrigorik/ga-beacon)

# Eproxy

Eproxy is a proxy that

* rewrites HTML, CSS, JavaScript``*`` and SVG``*``
* supports cookies``*``
* supports all HTTP methods 
* supports HTTPS
* supports HTTP Byte Serving using the ``Range`` header (if not rewriting)
* supports cache validation using ``If-Modified-Since`` and ``If-None-Match``
* caches content
* lets you customize the TCP/IP setup
* uses real parsers for URL rewriting, not brittle regular expressions
* attempts to be as secure as possible
* supports SOCKS proxies for your hardcore tunneling needs

## Features

* Runs stand-alone, in a JEE container (Tomcat or Jetty) and on Google App Engine
* Caches data in-memory, using Memcache (on Google App Engine) or Infinispan``*``
* Robust, parser-based HTML and CSS rewriting.

## Demo Site

[Demo site here](https://weizentortillas.appspot.com)

## Security status

[Eproxy does pretty well](https://weizentortillas.appspot.com/rnw-http/repo.eaio.com/leak.html) on the [HTTPLeaks](https://github.com/cure53/HTTPLeaks/) test.
Still, it is not tested enough yet. Do not use Eproxy yet if you rely on security.

## Installation

You'll need

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) (version 7 or greater)
* [Maven](https://maven.apache.org)
* [Git](http://www.git-scm.com/)

### Clone the repository

``git clone https://github.com/johannburkard/eproxy.git``

### Build it

``cd eproxy && mvn package``

### Stand-alone

If you want to try out Eproxy, simply start the ``.war`` file in the ``target`` directory:

``java -jar target/*##*.war``

Go to http://127.0.0.1:8080/index.html and press Ctrl-C when you're done.

### Tomcat or Jetty

Deploy the ``Eproxy##....war`` file.

### On Google App Engine

``mvn appengine:update``

## Configuration

The configuration can be found in ``src/main/resources/application.properties``. Most should be self-explanatory. The values have been tweaked for Google App Engine.

## History

The idea is based on [mirrorr](https://github.com/bslatkin/mirrorrr). Some of the code came from [media.io](http://media.io), a project called Delivrr (sadly gone) and [EAIO](http://eaio.com).

## mirrorr disadvantages

I have nothing against mirrorr but there are some problems with it:

* HTML rewriting is based on regular expressions.
* Only GET requests are supported.
* It (probably) doesn't support HTTPS.
* It only runs on Google App Engine, using Memcache as the cache.

``*`` Planned
