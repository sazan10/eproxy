[![Coverage Status](https://coveralls.io/repos/johannburkard/eproxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/johannburkard/eproxy?branch=master)

# eproxy

eproxy is a web proxy that

* rewrites HTML, CSS, SVG and JavaScript``*``
* supports cookies
* supports HTTPS
* supports HTTP Byte Serving independent of the origin server using the ``Range`` header (unless rewriting)
* supports cache validation using ``If-Modified-Since`` and ``If-None-Match``
* supports SOCKS proxies for your hardcore tunneling needs
* supports browsers with JavaScript disabled
* supports data: URIs``*``
* caches content in-memory and using Memcache (on Google App Engine)
* lets you customize the HTTP and TCP/IP setup
* uses robust, parser-based rewriting for HTML (using [NekoHTML](http://nekohtml.sourceforge.net/)) and SVG (using SAX)
* uses regular expression-based rewriting for CSS (sorry, but there are no robust Java CSS parsers)
* attempts to be as secure as possible
* runs stand-alone, in a JEE container (Tomcat or Jetty) and on Google App Engine
* has really good HTTP support

## Security status

eproxy does pretty well on the [HTTPLeaks](https://github.com/cure53/HTTPLeaks/) test.
Still, it is not tested enough yet. Do not use eproxy yet if you rely on security.

## Installation

### 0. Prerequisites

You'll need

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) (version 7 or greater)
* [Maven](https://maven.apache.org)
* [Git](http://www.git-scm.com/)

### 1. Get the Code

``git clone https://github.com/johannburkard/eproxy.git``

### 2. Build

``cd eproxy && mvn package``

### 3. Run or Deploy

#### Stand-alone

If you simply want to try out eproxy, start the ``.war`` file in the ``target`` directory:

``java -jar target/*##*.war``

Next, go to http://127.0.0.1:8080/index.html. Press Ctrl-C in the console when you're done.

#### Tomcat or Jetty

Deploy the ``target/eproxy##....war`` file.

#### Google App Engine

1. Create a project in Google Cloud
2. Change ``<application>`` to the name of your project in ``src/main/webapp/WEB-INF/appengine-web.xml``
3. ``mvn appengine:update``

## Configuration

The configuration can be found in ``src/main/resources/application.properties``.

## History

The idea is based on [mirrorr](https://github.com/bslatkin/mirrorrr). Some of the code came from [media.io](http://media.io), a project called Delivrr (sadly gone) and [EAIO](http://eaio.com/?utm_source=github&utm_medium=open-source&utm_campaign=eproxy).

``*`` Planned

[![Analytics](https://ga-beacon.appspot.com/UA-7427410-89/eproxy/README.md?pixel)](https://github.com/igrigorik/ga-beacon)