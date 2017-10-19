// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.links;

import java.nio.ByteBuffer;
import java.util.*;

import x2java.*;

import x2java.util.*;

public class SendBuffer {
    private byte[] headerBytes;
    private int headerLength;
    private Buffer buffer;

    public SendBuffer() {
        headerBytes = new byte[5];
        buffer = new Buffer();
    }

    public void close() {
        buffer.close();
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public byte[] getHeaderBytes() {
        return headerBytes;
    }

    public void setHeaderLength(int value) {
        headerLength = value;
    }

    public void listOccupiedBuffers(List<ByteBuffer> list) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBytes, 0, headerLength);

        Log.debug("listOccupied %d %d %d",
            byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());
        
        list.add(byteBuffer);

        buffer.listOccupiedBuffers(list);
    }

    public void reset() {
        headerLength = 0;
        buffer.trim();
    }
}
