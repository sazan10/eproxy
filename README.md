# eproxy

eproxy is a proxy that

* rewrites HTML, CSS, SVG and JavaScript``*``
* supports cookies
* supports all HTTP methods 
* supports HTTPS
* supports HTTP Byte Serving independent of the origin server using the ``Range`` header (unless rewriting)
* supports cache validation using ``If-Modified-Since`` and ``If-None-Match``
* supports SOCKS proxies for your hardcore tunneling needs
* supports users who have JavaScript disabled
* supports data: URIs``*``
* caches content in-memory, using Memcache (on Google App Engine) or Infinispan``*``
* lets you customize the TCP/IP setup
* uses robust, parser-based rewriting for HTML (using [NekoHTML](http://nekohtml.sourceforge.net/)) and SVG (using SAX)
* uses regex-based rewriting for CSS because there are no robust Java CSS parsers
* attempts to be as secure as possible
* runs stand-alone, in a JEE container (Tomcat or Jetty) and on Google App Engine
* has [really good HTTP support](https://redbot.org/?uri=https%3A%2F%2Fweizentortillas.appspot.com%2Frnw-http%2Fwww.n-tv.de%2F&req_hdr=User-Agent%3AMozilla%2F5.0+%28X11%3B+Ubuntu%3B+Linux+x86_64%3B+rv%3A44.0%29+Gecko%2F20100101+Firefox%2F44.0&req_hdr=Referer%3Ahttps%3A%2F%2Fweizentortillas.appspot.com%2F)

## [Demo Site](https://weizentortillas.appspot.com)

## Security status

[eproxy does pretty well](https://weizentortillas.appspot.com/rnw-http/repo.eaio.com/leak.html) on the [HTTPLeaks](https://github.com/cure53/HTTPLeaks/) test.
Still, it is not tested enough yet. Do not use eproxy yet if you rely on security.

## Installation

### 1. Prerequisites

You'll need

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) (version 7 or greater)
* [Maven](https://maven.apache.org)
* [Git](http://www.git-scm.com/)

### 2. Clone

``git clone https://github.com/johannburkard/eproxy.git``

### 3. Build

``cd eproxy && mvn package``

### 4. Run or Deploy

#### Stand-alone

If you want to try out eproxy, simply start the ``.war`` file in the ``target`` directory:

``java -jar target/*##*.war``

Go to http://127.0.0.1:8080/index.html and press Ctrl-C when you're done.

#### Tomcat or Jetty

Deploy the ``target/eproxy##....war`` file.

#### Google App Engine

``mvn appengine:update``

## Configuration

The configuration can be found in ``src/main/resources/application.properties``.

## History

The idea is based on [mirrorr](https://github.com/bslatkin/mirrorrr). Some of the code came from [media.io](http://media.io), a project called Delivrr (sadly gone) and [EAIO](http://eaio.com/?utm_source=github&utm_medium=open-source&utm_campaign=eproxy).

``*`` Planned

[![Analytics](https://ga-beacon.appspot.com/UA-7427410-89/eproxy/README.md?pixel)](https://github.com/igrigorik/ga-beacon)