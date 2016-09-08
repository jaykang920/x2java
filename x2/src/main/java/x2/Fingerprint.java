// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.io.IOException;

import x2.util.*;

/** Manages a fixed-length compact array of bit values. */
public class Fingerprint implements Comparable<Fingerprint> {
    private int block;     // primary (default) bit block
    private int[] blocks;  // additional bit blocks
    private final int length;

    private static boolean lessThanUnsigned(int x, int y) {
        return (x & 0x0ffffffffL) < (y & 0x0ffffffffL);
    }

    /** Gets the number of bits contained in this fingerprint. */
    public int getLength() { return length; }

    /** Gets the minimum number of bytes required to hold all the bits in this
     *  fingerprint.
     */
    private int lengthInBytes() {
        return ((length - 1) >> 3) + 1;
    }

    /** Constructs a new fingerprint object that can hold the specified number
     *  of bit values, which are initially set to <b>false</b>.
     *  @param length the number of bit values in the new fingerprint.
     *  @throws IllegalArgumentException when <code>length</code> is less than 0.
     */
    public Fingerprint(int length) {
        if (length < 0) {
            throw new IllegalArgumentException();
        }
        this.length = length;
        if (length > 32) {
            length -= 32;
            blocks = new int[((length - 1) >> 5) + 1];
        }
    }

    /** Constructs a new fingerprint object that contains bit values copied from
     *  the specified one.
     *  @param other a fingerprint object to copy from.
     */
    public Fingerprint(Fingerprint other) {
        block = other.block;
        if (other.blocks != null) {
            blocks = other.blocks.clone();
        }
        length = other.length;
    }

    /** Creates a new offset-based accessor to this fingerprint. */
    public Capo capo(int offset) {
        return new Capo(this, offset);
    }

    /** Clears all the bits in the fingerprint, setting them as <b>false</b>. */
    public void clear() {
        block = 0;
        if (blocks != null) {
            for (int i = 0; i < blocks.length; ++i) {
                blocks[i] = 0;
            }
        }
    }

    /** Compares this object with the specified object for order.
     *  Implements Comparable(T).compareTo interface.
     *  @param other a fingerprint object to be compared with this.
     *  @return a value that indicates the relative order of the Fingerprint
     *  objects being compared. Zero return value means that this is equal to
     *  <code>other</code>, while negative(positive) integer return value means
     *  that this is less(greater) than <code>other</code>.
     */
    public int compareTo(Fingerprint other) {
        if (other == this) {
            return 0;
        }
        if (length < other.length) {
            return -1;
        } else if (length > other.length) {
            return 1;
        }
        if (blocks != null) {
            for (int i = (blocks.length - 1); i >= 0; --i) {
                int thisBlock = blocks[i];
                int otherBlock = other.blocks[i];
                if (lessThanUnsigned(thisBlock, otherBlock)) {
                    return -1;
                } else if (lessThanUnsigned(otherBlock, thisBlock)) {
                    return 1;
                }
            }
        }
        if (lessThanUnsigned(block, other.block)) {
            return -1;
        } else if (lessThanUnsigned(other.block, block)) {
            return 1;
        }
        return 0;
    }

    /** Indicates whether the specified object is equal to this one.
     *  @param obj the reference object which to compare.
     *  @return <b>true</b> if <code>obj</code> is equal to this object;
     *  otherwise, <b>false</b>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Fingerprint)) {
            return false;
        }
        Fingerprint other = (Fingerprint)obj;
        if (length != other.length) {
            return false;
        }
        if (block != other.block) {
            return false;
        }
        if (blocks != null) {
            for (int i = 0; i < blocks.length; ++i) {
                if (blocks[i] != other.blocks[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Indicates whether the specified object is equivalent to this one.
     *  <p>
     *  A Fingerprint is said to be <i>equivalent</i> to the other when it
     *  covers all the bits set in the other.
     *  <p>
     *  Given two fingerprint objects x and y, x.equivalent(y) returns
     *  <b>true</b> if:
     *    <ul>
     *      <li>x.length is less than or equal to y.length</li>
     *      <li>All the bits set in x are also set in y</li>
     *    </ul>
     *  @param other the reference object which to compare.
     *  @return <b>true</b> if <code>other</code> is equivalent to this object;
     *  otherwise, <b>false</b>.
     */
    public boolean equivalent(Fingerprint other) {
        if (other == this) {
            return true;
        }
        if (length > other.length) {
            return false;
        }
        if ((block & other.block) != block) {
            return false;
        }
        if (blocks != null) {
            for (int i = 0; i < blocks.length; ++i) {
                int thisBlock = blocks[i];
                if ((thisBlock & other.blocks[i]) != thisBlock) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Gets the bit value at the specified index.
     *  @param index the zero-based index of the bit to get.
     *  @return the bit value at the position <code>index</code>.
     *  @throws IndexOutOfBoundsException when <code>index</code> is less than 0,
     *  or when <code>index</code> is greater than or equal to the length of the
     *  fingerprint.
     */
    public boolean get(int index) {
        if (index < 0 || length <= index) {
            Log.debug("Fingerprint.get length=%d index=%d", length, index);
            throw new IndexOutOfBoundsException();
        }
        if ((index & (-1 << 5)) != 0) {  // index >= 32
            index -= 32;
            return ((blocks[index >> 5] & (1 << index)) != 0);
        }
        return ((block & (1 << index)) != 0);
    }

    /** Returns the hash code for this object.
     *  @return an integer that can serve as the hash code for this object.
     */
    @Override
    public int hashCode() {
        Hash hash = new Hash();
        hash.update(length);
        hash.update(block);
        if (blocks != null) {
            for (int i = 0; i < blocks.length; ++i) {
                hash.update(blocks[i]);
            }
        }
        return hash.code();
    }

  /*
  public void Dump(Buffer buffer) {
    //buffer.WriteUInt29(length);
    int numBytes = ((length - 1) >> 3) + 1;
    int count = 0;
    foreach (int block in blocks) {
      for (int j = 0; (j < 4) && (count < numBytes); ++j, ++count) {
        buffer.Write((byte)(block >> (j << 3)));
      }
    }
  }

  public void Load(Buffer buffer) {
    //int length;
    //buffer.ReadUInt29(out length);
    //if (this.length != length) {
    //  throw new System.IO.InvalidDataException();
    //}
    int numBytes = ((length - 1) >> 3) + 1;
    int count = 0;
    for (int i = 0; i < blocks.Length; ++i) {
      blocks[i] = 0;
      for (int j = 0; (j < 4) && (count < numBytes); ++j, ++count) {
        blocks[i] |= ((int)buffer.ReadByte() << (j << 3));
      }
    }
  }
  */

    /** Sets the bit at the specified index.
     *  @param index the zero-based index of the bit to set.
     *  @throws IndexOutOfBoundsException when <code>index</code> is less than 0,
     *  or when <code>index</code> is greater than or equal to the length of the
     *  fingerprint.
     */
    public void touch(int index) {
        if (index < 0 || length <= index) {
            throw new IndexOutOfBoundsException();
        }
        if ((index & (-1 << 5)) != 0) {  // index >= 32
            index -= 32;
            blocks[index >> 5] |= (1 << index);
        }
        block |= (1 << index);
    }

    /** Clears the bit at the specified index.
     *  @param index the zero-based index of the bit to clear.
     *  @throws IndexOutOfBoundsException when <code>index</code> is less than 0,
     *  or when <code>index</code> is greater than or equal to the length of the
     *  Fingerprint.
     */
    public void wipe(int index) {
        if (index < 0 || length <= index) {
            throw new IndexOutOfBoundsException();
        }
        if ((index & (-1 << 5)) != 0) {  // index >= 32
            index -= 32;
            blocks[index >> 5] &= ~(1 << index);
        }
        block &= ~(1 << index);
    }

    // Serialization

    public void deserialize(Deserializer deserializer) throws IOException {
        int length = deserializer.readNonnegativeInt();
        int lengthInBytes = ((length - 1) >> 3) + 1;
        int lengthInBlocks = ((lengthInBytes - 1) >> 2) + 1;
        int effectiveBytes = lengthInBytes();

        int count = 0;
        block = 0;
        for (int i = 0; (i < 4) && (count < lengthInBytes); ++i, ++count) {
            byte b = deserializer.readByte();
            if (count < effectiveBytes) {
                block |= (b << (i << 3));
            }
        }
        for (int i = 0; i < lengthInBlocks; ++i) {
            int word = 0;
            for (int j = 0; (j < 4) && (count < lengthInBytes); ++j, ++count) {
                byte b = deserializer.readByte();
                if (count < effectiveBytes) {
                    word |= (b << (j << 3));
                }
            }
            if (blocks != null && i < blocks.length) {
                blocks[i] = word;
            }
        }
    }

    public int length() {
        return Serializer.lengthNonnegativeInt(length) + lengthInBytes();
    }

    public void serialize(Serializer serializer) {
        serializer.writeNonnegativeInt(length);
        int lengthInBytes = lengthInBytes();

        int count = 0;
        for (int i = 0; (i < 4) && (count < lengthInBytes); ++i, ++count) {
            serializer.writeByte((byte)(block >> (i << 3)));
        }
        if (blocks == null) {
            return;
        }
        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; (j < 4) && (count < lengthInBytes); ++j, ++count) {
                serializer.writeByte((byte)(blocks[i] >> (j << 3)));
            }
        }
    }
}
