// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.links.socket;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import x2java.links.*;
import x2java.util.*;

public class TcpClient extends ClientLink implements Runnable {
    InetSocketAddress socketAddress;
    SocketChannel socketChannel;
    Selector selector;

    public TcpClient(String name) {
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

    public void connect(String host, int port) throws IOException {
        connect(new InetSocketAddress(host, port));
    }

    public void connect(InetSocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        new Thread(this).start();
    }

    private void onConnect(SelectionKey key) {
        SocketChannel channel = (SocketChannel)key.channel();
        try {
            channel.finishConnect();
        } catch (IOException ioe) {
            Log.info("%s connection error %s", name(), ioe.toString());
            key.cancel();
            return;
        }

        Log.info("%s connected", name());
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        LinkSession session = new TcpSession(this, channel);
        onConnectInternal(session);
    }

    private void onRead(SelectionKey key) {
        ((TcpSession)session).onRead(key);
    }

    private void onWrite(SelectionKey key) {
        ((TcpSession)session).onWrite(key);
    }

    public void run() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            socketChannel.connect(socketAddress);

            Log.info("%s connecting to %s", name(), socketAddress.toString());

            while (true) {
                if (selector.select() == 0) {
                    Thread.sleep(1);
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isConnectable()) {
                        onConnect(key);
                    } else if (key.isReadable()) {
                        onRead(key);
                    } else if (key.isWritable()) {
                        onWrite(key);
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
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    Log.error(e.toString());
                } finally {
                    socketChannel = null;
                }
            }
        }
    }
}
