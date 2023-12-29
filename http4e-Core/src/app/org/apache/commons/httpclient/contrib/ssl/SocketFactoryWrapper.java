/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.httpclient.contrib.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 * Author: Mark Claassen
 * <p>
 * Uses some code from EasySSLProtocolSocketFactory.java
 * 
 * <p>
 * Wraps a SSLSocketFactory with a SecureProtocolSocketFactory.
 * <p>
 * This was designed to make HttpClient work in situations where an application is being deployed by
 * Java Web Start. In these cases, SSL connections are negotiated by webstart implementations of the
 * KeyManager and TrustManager. Wrapping the socket factory obtained from
 * HttpsURLConnection.getDefaultSocketFactory allows the use of HttpClient while still leveraging
 * Java Web Start's handling of SSL certificates
 */
public class SocketFactoryWrapper implements SecureProtocolSocketFactory {

    private final SSLSocketFactory socketFactory;

    public SocketFactoryWrapper(final SSLSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort)
            throws IOException, UnknownHostException {
        return socketFactory.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        // Based on code from EasySSLProtocolSocketFactory.java
        Socket rval;
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        final int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            rval = socketFactory.createSocket(host, port, localAddress, localPort);
        } else {
            rval = socketFactory.createSocket();
            final SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            final SocketAddress remoteaddr = new InetSocketAddress(host, port);
            rval.bind(localaddr);
            rval.connect(remoteaddr, timeout);
        }
        return rval;
    }

    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException, UnknownHostException {
        return socketFactory.createSocket(socket, host, port, autoClose);
    }

}
