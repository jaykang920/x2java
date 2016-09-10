// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links.socket;

import java.nio.ByteBuffer;
import java.nio.channels.*;

import x2.*;
import x2.links.*;
import x2.util.*;

public class TcpSession extends LinkSession {
    protected SocketChannel channel;

    public TcpSession(SessionBasedLink link, SocketChannel channel) {
        super(link);
        this.channel = channel;
    }

    /** Returns the underlying socket channel object. */
    public SocketChannel channel() {
        return channel;
    }

    @Override
    public void close() {
        if (closed) { return; }

        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                // log
            }
        }

        super.close();
    }

    public void onRead(SelectionKey key) {
        Log.info("onRead");

        // receiveInternal
        rxBufferList.clear();
        rxBuffer.listAvailableBuffers(rxBufferList);

        Log.debug("%s num recv buffers %d", link.name(), rxBufferList.size());

        for (int i = 0, count = rxBufferList.size(); i < count; ++i) {
            ByteBuffer byteBuffer = rxBufferList.get(i);
            Log.debug("rxBuffer %d %d %d", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());
        }

        SocketChannel channel = (SocketChannel)key.channel();

        ByteBuffer[] byteBuffers = new ByteBuffer[rxBufferList.size()];
        rxBufferList.toArray(byteBuffers);
        
        try {
            long bytesRead = channel.read(byteBuffers);

            Log.debug("%s bytesRead %d", link.name(), bytesRead);

            if (bytesRead <= 0) {
                Log.info("%s %d closed", link.name(), handle);
                // onclose
            }

            onReceiveInternal((int)bytesRead);
        } catch (Exception e) {
            Log.warn("%s %d recv error %s", link.name(), handle, e.toString());
            //onDisconnect();
        }
    }

    public void onWrite(SelectionKey key) {

    }

    @Override
    protected void buildHeader(SendBuffer sendBuffer) {
        Log.debug("%s buffer length %d", link.name(), sendBuffer.getBuffer().length());

        int header = sendBuffer.getBuffer().length() << 1;

        Log.debug("%s header %d", link.name(), header);

        sendBuffer.setHeaderLength(
            Serializer.writeVariableUInt(sendBuffer.getHeaderBytes(), header));
    }

    @Override
    protected boolean parseHeader() {
        int header;
        int headerLength;
        try {
            Mutable<Integer> length = new Mutable<Integer>();
            header = Deserializer.readVariableUInt(rxBuffer, length);
            headerLength = length.get();

            Log.debug("%s header %d headerLength %d", link.name(), header, headerLength);

        } catch (Exception e) {
            // need more to start
            return false;
        }
        rxBuffer.shrink(headerLength);
        lengthToReceive = (header >> 1) & 0x7fffffff;

        Log.debug("%s lengthToReceive %d", link.name(), lengthToReceive);

        return true;
    }

    @Override
    protected void sendInternal() {
        Log.debug("%s sendInternal() with %d buffer(s)", link.name(), txBufferList.size());

        try {
            ByteBuffer[] byteBuffers = new ByteBuffer[txBufferList.size()];
            txBufferList.toArray(byteBuffers);

            for (int i = 0; i < byteBuffers.length; ++i) {
                ByteBuffer byteBuffer = byteBuffers[i];

                byte[] array = byteBuffer.array();
                int offset = byteBuffer.arrayOffset() + byteBuffer.position();
                int length = byteBuffer.limit();

                Log.debug("%d %d", offset, length);

                for (int j = 0; j < length; ++j) {
                    byte b = array[offset + j];
                    Log.debug("%02x ", b);
                }
            }

            channel.write(byteBuffers);
        } catch (Exception e) {
            Log.warn("%s %d send error %s", link.name(), handle, e.toString());
            //onDisconnect();
        }
    }
}
