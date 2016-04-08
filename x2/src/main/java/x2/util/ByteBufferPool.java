// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.util;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.*;

import x2.Buffer;

/** Manages a pool of fixed-length (2^n) ByteBuffer objects. */
public class ByteBufferPool {
    private static final int chunkSize = (1 << Buffer.SIZE_EXPONENT * 2);
    private static final int segmentSize = (1 << Buffer.SIZE_EXPONENT);

    private static List<SegmentedByteBuffer> pools;

    private static ReadWriteLock rwlock;

    static {
        pools = new ArrayList<SegmentedByteBuffer>();
        rwlock = new ReentrantReadWriteLock();

        Lock wlock = rwlock.writeLock();
        try {
            wlock.lock();
            pools.add(new SegmentedByteBuffer(chunkSize, segmentSize));
        }
        finally {
            wlock.unlock();
        }
    }

    /** Private constructor to prohibit explicit instantiation. */
    private ByteBufferPool() { }

    /** Acquires an available ByteBuffer from the pool. */
    public static ByteBuffer acquire() {
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            for (int i = 0, size = pools.size(); i < size; ++i) {
                ByteBuffer result = pools.get(i).acquire();
                if (result != null) {
                    return result;
                }
            }
            Lock wlock = rwlock.writeLock();
            wlock.lock();
            try {
                for (int i = 0, size = pools.size(); i < size; ++i) {
                    ByteBuffer result = pools.get(i).acquire();
                    if (result != null) {
                        return result;
                    }
                }
                SegmentedByteBuffer pool =
                        new SegmentedByteBuffer(chunkSize, segmentSize);
                pools.add(pool);
                return pool.acquire();

            }
            finally {
                wlock.unlock();
            }
        }
        finally {
            rlock.unlock();
        }
    }

    /** Returns the specified ByteBuffer back to the pool. */
    public static void release(ByteBuffer byteBuffer) {
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            for (int i = 0, size = pools.size(); i < size; ++i) {
                if (pools.get(i).release(byteBuffer)) {
                    return;
                }
            }
        }
        finally {
            rlock.unlock();
        }
    }
}
