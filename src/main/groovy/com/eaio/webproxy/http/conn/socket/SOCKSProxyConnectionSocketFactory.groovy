package com.eaio.webproxy.http.conn.socket

import groovy.transform.TupleConstructor

import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket

import org.apache.http.HttpHost
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.protocol.HttpContext

/**
 * {@link ConnectionSocketFactory} that tunnels through a SOCKS proxy.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@TupleConstructor
class SOCKSProxyConnectionSocketFactory {

    @Delegate
    ConnectionSocketFactory delegate

    Proxy proxy

    /**
     * @see org.apache.http.conn.socket.ConnectionSocketFactory#createSocket(org.apache.http.protocol.HttpContext)
     */
    Socket createSocket(HttpContext context) throws IOException {
        new Socket(proxy)
    }

    Socket connectSocket(
            int connectTimeout,
            Socket socket,
            HttpHost host,
            InetSocketAddress remoteAddress,
            InetSocketAddress localAddress,
            HttpContext context) throws IOException {
        try {
            delegate.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context)
        }
        catch (SocketException ex) {
            if (ex.message == 'Connection refused') {
                throw new SocketException("Could not connect to ${remoteAddress} through ${proxy}")
            }
            throw ex
        }
    }

}
