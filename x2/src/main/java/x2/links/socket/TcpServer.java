// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links.socket;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import x2.links.*;
import x2.util.*;

public class TcpServer extends ServerLink implements Runnable {
    private InetSocketAddress socketAddress;
    private volatile ServerSocketChannel ssc;
    Selector selector;

    public TcpServer(String name) {
        super(name);
    }

    @Override
    public void close() {
        if (closed) { return; }

        try {
            selector.close();
        } catch (IOException e) {
            //
        }
        Log.info("%s close", name());
        
        super.close();
    }

    public void listen(int port) throws IOException {
        listen(new InetSocketAddress(port));
    }

    public void listen(InetSocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        new Thread(this).start();
    }

    public void run() {
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);

            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            ssc.socket().bind(socketAddress);

            Log.info("%s listening on %d", name(), socketAddress.getPort());

            while (true) {
                if (selector.select() == 0) {
                    Thread.sleep(1);
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel clientChannel;
                        try {
                            clientChannel = ssc.accept();
                        } catch (IOException ioe) {
                            Log.info("%s accept error %s", name(), ioe.toString());
                            continue;
                        }
                        clientChannel.configureBlocking(false);
                        
                        Log.info("%s accepted from %s", name(), clientChannel.getRemoteAddress());

                        clientChannel.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        //
                    }

                }
            }
        }
        catch (ClosedSelectorException cse) {
            //
        }
        catch (Exception e) {
            Log.error(e.toString());
        }
        finally {
            if (selector != null && selector.isOpen()) {
                try {
                    selector.close();
                } catch (Exception e) {
                    Log.error(e.toString());
                } finally {
                    selector = null;
                }
            }

            // close client

            if (ssc != null) {
                try {
                    ssc.close();
                } catch (IOException e) {
                    Log.error(e.toString());
                } finally {
                    ssc = null;
                }
            }
        }
    }
}
