// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import x2.util.Hash;

/** Manages a fixed-length compact array of bit values. */
public class Fingerprint implements Comparable<Fingerprint> {
    private int block;     // primary (default) bit block
    private int[] blocks;  // additional bit blocks
    private final int length;

    private static boolean lessThanUnsigned(int x, int y) {
        return (x & 0x0ffffffffL) < (y & 0x0ffffffffL);
    }

    /** Gets the number of bits contained in this fingerprint. */
    public int length() { return length; }

    /** Gets the minimum number of bytes required to hold all the bits in this
     *  fingerprint.
     */
    private int lengthInBytes() {
        return ((length - 1) >> 3) + 1;
    }

    /** Constructs a new fingerprint object that can hold the specified number
     *  of bit values, which are initially set to <b>false</b>.
     *  @param length the number of bit values in the new fingerprint.
     *  @throws IllegalArgumentException when <code>length</length> is less than 0.
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
     *  the specified fingerprint.
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

    public void deserialize(Deserializer deserializer) {

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
     *    <list type="bullet">
     *      <item>x.length is less than or equal to y.length</item>
     *      <item>All the bits set in x are also set in y</item>
     *    </list>
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

    /** Offset-based accessor for the underlying fingerprint. */
    public class Capo {
        private Fingerprint fingerprint;
        private int offset;

        /** Constructs a new Capo object with the specified fingerprint and
         *  offset.
         *  @param fingerprint a Fingerprint object to access.
         *  @param position an integer offset to apply constantly.
         */
        public Capo(Fingerprint fingerprint, int offset) {
            this.fingerprint = fingerprint;
            this.offset = offset;
        }

        /** Gets the bit value at the specified index in the underlying
         *  fingerprint, applying the offset.
         *  <p>
         *  This method does not throw on upper-bound overrun. If the calculated
         *  position index (<code>offset</code> + <code>index</code>) is greater
         *  than or equal to the length of the underlying fingerprint, it simply
         *  returns <b>false</b>.
         *  @param index the zero-based index of the bit to get.</param>
         *  @return the bit value at the position (<code>offset</code> + <code>index</c>).
         */
        public boolean get(int index) {
            int effectiveIndex = offset + index;
            if (effectiveIndex >= fingerprint.length()) {
                return false;
            }
            return fingerprint.get(effectiveIndex);
        }
    }
}

/** Extends Fingerprint class to hold an additional reference count. */
class Slot extends Fingerprint {
    private volatile int refCount;

    /** Initializes a new instance of the Slot class that contains bit values
     *  copied from the specified Fingerprint.
     *  @param fingerprint  a Fingerprint object to copy from.
     */
    public Slot(Fingerprint fingerprint) {
        super(fingerprint);
        refCount = 1;
    }

    /** Increases the reference count of this Slot.
     *  @returns  the resultant reference count.
     */
    public int addRef() {
        return ++refCount;
    }

    /** Decreases the reference count of this Slot.
     *  @returns  the resultant reference count.
     */
    public int removeRef() {
        return --refCount;
    }
}
