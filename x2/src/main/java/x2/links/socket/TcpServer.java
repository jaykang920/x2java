// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links.socket;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

import x2.links.*;

public class TcpServer extends ServerLink {
    private ServerSocketChannel ssc;

    public TcpServer(String name) {
        super(name);
    }

    @Override
    public void close() {

    }

    public void listen(int port) throws IOException {
        listen(new InetSocketAddress(port));
    }

    public void listen(InetSocketAddress socketAddress) throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.socket().bind(socketAddress);
        ssc.configureBlocking(false);
    }
}
