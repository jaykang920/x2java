// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.util;

import java.nio.ByteBuffer;
import java.util.Stack;

/** Manages a single large byte buffer as if it's a pool of smaller buffers. */
public class SegmentedByteBuffer {
    private int chunkSize;
    private int segmentSize;

    private ByteBuffer buffer;
    private int offset;
    private Stack<ByteBuffer> available;

    private final Object syncRoot = new Object();

    /** Constructs a new SegmentedByteBuffer object with the specified size
     *  parameters.
     */
    public SegmentedByteBuffer(int chunkSize, int segmentSize) {
        this.chunkSize = chunkSize;
        this.segmentSize = segmentSize;

        buffer = ByteBuffer.allocate(chunkSize);
        available = new Stack<ByteBuffer>();
    }

    /** Acquires an available ByteBuffer object, or null if not available. */
    public ByteBuffer acquire() {
        synchronized (available) {
            if (available.size() > 0) {
                return available.pop();
            }
        }

        int position;
        synchronized (syncRoot) {
            if ((chunkSize - segmentSize) < offset) {
                return null;
            }
            position = offset;
            offset += segmentSize;
        }
        buffer.position(position);
        buffer.limit(position + segmentSize);
        return buffer.slice();
    }

    /** Tries to return the specified ByteBuffer back to the pool.
     *  @return true if successful, false if the specified object does not
     *  belong to this pool.
     */
    public boolean release(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            throw new IllegalArgumentException();
        }
        if (byteBuffer.array() != buffer.array()) {
            return false;
        }
        synchronized (available) {
            byteBuffer.position(0);
            byteBuffer.limit(segmentSize);
            available.push(byteBuffer);
        }
        return true;
    }
}
