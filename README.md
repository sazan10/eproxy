[![Coverage Status](https://coveralls.io/repos/johannburkard/eproxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/johannburkard/eproxy?branch=master) [![Analytics](https://ga-beacon.appspot.com/UA-7427410-88/eproxy/README.md?pixel)](https://github.com/igrigorik/ga-beacon)

# Eproxy

Eproxy is a proxy that

* rewrites HTML, CSS, JavaScript``*`` and SVG``*``
* supports cookies``*``
* supports all HTTP methods 
* supports HTTPS
* supports HTTP Byte Serving using the ``Range`` header (unless rewriting)
* supports cache validation using ``If-Modified-Since`` and ``If-None-Match``
* supports SOCKS proxies for your hardcore tunneling needs
* supports users who have JavaScript disabled
* caches content in-memory, using Memcache (on Google App Engine) or Infinispan``*``
* lets you customize the TCP/IP setup
* uses robust, parser-based rewriting of HTML (using [NekoHTML](http://nekohtml.sourceforge.net/)), CSS (using [CSS Parser](http://cssparser.sourceforge.net/)) and SVG
* attempts to be as secure as possible
* runs stand-alone, in a JEE container (Tomcat or Jetty) and on Google App Engine
* has [really good HTTP support](https://redbot.org/?uri=https%3A%2F%2Fweizentortillas.appspot.com%2Frnw-http%2Fwww.n-tv.de%2F&req_hdr=User-Agent%3AMozilla%2F5.0+%28X11%3B+Ubuntu%3B+Linux+x86_64%3B+rv%3A44.0%29+Gecko%2F20100101+Firefox%2F44.0&req_hdr=Referer%3Ahttps%3A%2F%2Fweizentortillas.appspot.com%2F)

## [Demo Site](https://weizentortillas.appspot.com)

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

The configuration can be found in ``src/main/resources/application.properties``.

## History

The idea is based on [mirrorr](https://github.com/bslatkin/mirrorrr). Some of the code came from [media.io](http://media.io), a project called Delivrr (sadly gone) and [EAIO](http://eaio.com).

``*`` Planned
