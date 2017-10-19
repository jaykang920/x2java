// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import x2java.util.*;

/** A variable-length byte buffer that grows uni-directionally to minimize the
 *  need for copy. The capacity of the Buffer is limited to a multiple of a
 *  power of 2.
 */
public class Buffer {
    public static final int SIZE_EXPONENT = 12;
    public static final int REMAINDER_MASK = ~(~0 << SIZE_EXPONENT);
    public static final int BLOCK_SIZE = (1 << SIZE_EXPONENT);

    private List<ByteBuffer> blocks;

    private ByteBuffer current;
    private int currentIndex;

    private int position;
    private int back;
    private int front;

    private int marker;

    /** Constructs a new buffer object with a single initial block. */
    public Buffer() {
        blocks = new ArrayList<ByteBuffer>();
        blocks.add(ByteBufferPool.acquire());

        currentIndex = 0;
        current = blocks.get(currentIndex);

        position = 0;
        back = 0;
        front = 0;

        marker = -1;
    }

    private void blockFeed() {
        if (((position & REMAINDER_MASK) == 0) &&
                ((position & ~REMAINDER_MASK) != 0)) {
            current = blocks.get(++currentIndex);
        }
    }

    /** Returns the maximum capacity of the buffer. */
    public int capacity() {
        return (BLOCK_SIZE * blocks.size());
    }

    /** Returns the blocks back to the pool. */
    public void close() {
        if (blocks.size() == 0)
        {
            return;
        }
        for (int i = 0, size = blocks.size(); i < size; ++i)
        {
            ByteBufferPool.release(blocks.get(i));
        }
        blocks.clear();
        current = null;
    }

    public void copyFrom(byte[] buffer, int offset, int length) {
        ensureCapacityToWrite(length);
        int blockIndex = position >> SIZE_EXPONENT;
        int dstOffset = position & REMAINDER_MASK;
        int bytesToCopy, bytesCopied = 0;
        while (bytesCopied < length) {
            bytesToCopy = Math.min(BLOCK_SIZE - dstOffset, length - bytesCopied);
            ByteBuffer block = blocks.get(blockIndex++);

            block.position(dstOffset);
            block.put(buffer, offset + bytesCopied, bytesToCopy);

            dstOffset = 0;
            bytesCopied += bytesToCopy;
        }
        setPosition(position() + length);
    }

    private void copyTo(byte[] buffer, int offset, int length, int position) {
        int blockIndex = position >> SIZE_EXPONENT;
        int srcOffset = position & REMAINDER_MASK;
        int bytesToCopy, bytesCopied = 0;
        while (bytesCopied < length) {
            bytesToCopy = Math.min(BLOCK_SIZE - srcOffset, length - bytesCopied);
            ByteBuffer block = blocks.get(blockIndex++);

            block.position(srcOffset);
            block.get(buffer, offset + bytesCopied, bytesToCopy);

            srcOffset = 0;
            bytesCopied += bytesToCopy;
        }
    }

    public void checkLengthToRead(int numBytes) throws IOException {
        int limit = (marker >= 0) ? marker : back;
        if ((position + numBytes) > limit) {
            throw new IOException();
        }
    }

    public void ensureCapacityToWrite(int numBytes) {
        int required = position + numBytes;
        while (required >= capacity()) {
            blocks.add(ByteBufferPool.acquire());
        }
        if (required > back) {
            back = required;
        }
    }

    /** Relative get method. */
    public byte get() {
        blockFeed();
        return current.get(position++ & REMAINDER_MASK);
    }

    /** Relative bulk get method. */
    public void get(byte[] dst, int offset, int length) throws IOException {
        checkLengthToRead(length);
        copyTo(dst, offset, length, position);
        setPosition(position() + length);
    }

    /** Absolute get method. */
    public byte get(int index) {
        index += front;
        ByteBuffer block = blocks.get(index >> SIZE_EXPONENT);
        return block.get(index & REMAINDER_MASK);
    }

    /** Relative put method. */
    public void put(byte b) {
        blockFeed();
        current.put(position++ & REMAINDER_MASK, b);
    }

    /** Relative bulk put method. */
    public void put(byte[] src, int offset, int length) {
        copyFrom(src, offset, length);
    }

    /** Absolute put method. */
    public void put(int index, byte b) {
        index += front;
        ByteBuffer block = blocks.get(index >> SIZE_EXPONENT);
        block.put(index & REMAINDER_MASK, b);
    }

    /** Checks whether the buffer is empty(length == 0). */
    public boolean isEmpty() {
        return (front == back);
    }

    /** Returns the length of the buffered bytes. */
    public int length() {
        return (back - front);
    }

    public void listOccupiedBuffers(List<ByteBuffer> list) {
        listBuffers(list, front, back);
    }

    public void listStartingBuffers(List<ByteBuffer> list, int length) {
        listBuffers(list, front, front + length);
    }

    public void listEndingBuffers(List<ByteBuffer> list, int length) {
        listBuffers(list, back - length, back);
    }

    private void listBuffers(List<ByteBuffer> list, int begin, int end) {
        int beginIndex = begin >> SIZE_EXPONENT;
        int beginOffset = begin & REMAINDER_MASK;
        int endIndex = end >> SIZE_EXPONENT;
        int endOffset = end & REMAINDER_MASK;

        ByteBuffer block = blocks.get(beginIndex);
        block.position(beginOffset);
        if (beginIndex == endIndex) {
            block.limit(endOffset);
            list.add(block);
            return;
        }
        block.limit(BLOCK_SIZE);
        list.add(block);

        for (int i = beginIndex + 1; i < endIndex; ++i) {
            block = blocks.get(i);
            block.rewind();
            block.limit(BLOCK_SIZE);
            list.add(block);
        }

        if (endOffset != 0) {
            block = blocks.get(endIndex);
            block.rewind();
            block.limit(endOffset);
            list.add(block);
        }
    }

    public void listAvailableBuffers(List<ByteBuffer> list) {
        int numWholeBlocks = (capacity() - back) >> SIZE_EXPONENT;
        if (numWholeBlocks < 1) {
            blocks.add(ByteBufferPool.acquire());
        }

        int backIndex = back >> SIZE_EXPONENT;
        int backOffset = back & REMAINDER_MASK;
        ByteBuffer block = blocks.get(backIndex);
        block.position(backOffset);
        block.limit(BLOCK_SIZE);
        list.add(block);

        for (int i = backIndex + 1, size = blocks.size(); i < size; ++i) {
            block = blocks.get(i);
            block.rewind();
            block.limit(BLOCK_SIZE);
            list.add(block);
        }
    }

    public void markToRead(int lengthToRead) {
        if ((front + lengthToRead) > back) {
            throw new IllegalArgumentException();
        }
        marker = front + lengthToRead;
    }

    /** Returns the current zero-based position. */
    public int position() {
        return (position - front);
    }

    public void reset() {
        setPosition(0);
        back = front;
    }

    /** Alias of setPosition(0). */
    public void rewind() {
        setPosition(0);
    }

    /** Sts the current zero-based position. */
    public void setPosition(int value) {
        int adjusted = value + front;
        if (adjusted < front || back < adjusted) {
            throw new IndexOutOfBoundsException();
        }
        position = adjusted;
        // Update the current block.
        int blockIndex = position >> SIZE_EXPONENT;
        if ((blockIndex != 0) && ((position & REMAINDER_MASK) == 0)) {
            --blockIndex;
        }
        if (blockIndex != currentIndex) {
            currentIndex = blockIndex;
            current = blocks.get(currentIndex);
        }
    }

    public void shrink(int numBytes) {
        if ((front + numBytes) > back) {
            throw new IllegalArgumentException();
        }
        front += numBytes;
        if (position < front) {
            setPosition(0);
        }
    }

    public void stretch(int numBytes) {
        if ((back + numBytes) > capacity()) {
            throw new IllegalArgumentException();
        }
        back += numBytes;
    }

    /** Returns a byte array containing all the bytes in this buffer. */
    public byte[] toArray() {
        byte[] result = new byte[length()];
        copyTo(result, 0, length(), front);
        return result;
    }

    public void trim() {
        int index, count;
        if (marker >= 0) {
            if (position < marker) {
                setPosition(marker - front);
            }
            marker = -1;
        }

        if (position == back) {
            index = 1;
            count = blocks.size() - 1;
            front = back = 0;
        }
        else {
            index = 0;
            count = position >> SIZE_EXPONENT;
            if (count >= blocks.size()) {
                count = blocks.size() - 1;
            }
            back -= (BLOCK_SIZE * count);
            front = position & REMAINDER_MASK;
        }

        if (count > 0) {
            List<ByteBuffer> blocksToRemove = blocks.subList(index, index + count);
            for (int i = 0; i < blocksToRemove.size(); ++i) {
                ByteBufferPool.release(blocksToRemove.get(i));
            }
            blocksToRemove.clear();
        }
        setPosition(0);
    }
}
