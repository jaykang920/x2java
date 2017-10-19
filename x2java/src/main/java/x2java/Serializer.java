// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

import java.util.*;

/** Binary wire format serializer. */
public final class Serializer {
    private Buffer buffer;

    /** Constructs a new serializer object that works on the specified buffer. */
    public Serializer(Buffer buffer) {
        this.buffer = buffer;
    }

    // Write/length methods for primitive types

    /** Returns the number of bytes required to encode a boolean value. */
    public static int lengthBoolean(boolean value) {
        return 1;
    }

    /** Encodes a boolean value into the underlying buffer. */
    public void writeBoolean(boolean value) {
        buffer.ensureCapacityToWrite(1);
        buffer.put((byte)(value ? 1 : 0));
    }

    /** Returns the number of bytes required to encode a single byte. */
    public static int lengthByte(byte value) {
        return 1;
    }

    /** Encodes a single byte into the underlying buffer. */
    public void writeByte(byte value) {
        buffer.ensureCapacityToWrite(1);
        buffer.put(value);
    }

    /** Returns the number of bytes required to encode a 16-bit signed integer */
    public static int lengthShort(short value) {
        return 2;
    }

    /** Encodes a 16-bit signed integer into the underlying buffer. */
    public void writeShort(short value) {
        buffer.ensureCapacityToWrite(2);
        buffer.put((byte)(value >> 8));
        buffer.put((byte)value);
    }

    /** Returns the number of bytes required to encode a 32-bit signed integer. */
    public static int lengthInt(int value) {
        return lengthVariableInt(value);
    }

    /** Encodes a 32-bit signed integer into the underlying buffer. */
    public void writeInt(int value) {
        writeVariableInt(value);
    }

    /** Returns the number of bytes required to encode a 32-bit non-negative
     *  integer.
     */
    public static int lengthNonnegativeInt(int value) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        return lengthVariableUInt(value);
    }

    /** Encodes a 32-bit non-negative integer into the underlying buffer. */
    public void writeNonnegativeInt(int value) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        writeVariableUInt(value);
    }

    /** Returns the number of bytes required to encode a 64-bit signed integer. */
    public static int lengthLong(long value) {
        return lengthVariableLong(value);
    }

    /** Encodes a 64-bit signed integer into the underlying buffer. */
    public void writeLong(long value) {
        writeVariableLong(value);
    }

    /** Returns the number of bytes required to encode a 32-bit floating-point
     *  number.
     */
    public static int lengthFloat(float value) {
        return 4;
    }

    /** Encodes a 32-bit floating-point number into the underlying buffer. */
    public void writeFloat(float value) {
        writeFixedInt(Float.floatToIntBits(value));
    }

    /** Returns the number of bytes required to encode a 64-bit floating-point
     *  number.
     */
    public static int lengthDouble(double value) {
        return 8;
    }

    /** Encodes a 64-bit floating-point number into the underlying buffer. */
    public void writeDouble(double value) {
        writeFixedLong(Double.doubleToLongBits(value));
    }

    /** Returns the number of bytes required to encode a text string. */
    public static int lengthString(String value) {
        int length = lengthUtf8(value);
        return lengthNonnegativeInt(length) + length;
    }

    /** Returns the number of bytes required to encode the specified string in
     *  UTF-8 encoding. */
    private static int lengthUtf8(String value) {
        int length = 0;
        if (value != null) {
            for (int i = 0, count = value.length(); i < count; ++i) {
                char c = value.charAt(i);

                if ((c & 0xff80) == 0) { ++length; }
                else if ((c & 0xf800) != 0) { length += 3; }
                else { length += 2; }
            }
        }
        return length;
    }

    /** Encodes a text string into the underlying buffer. */
    public void writeString(String value) {
        // UTF-8 encoding
        int length = lengthUtf8(value);
        writeNonnegativeInt(length);
        if (length == 0)
        {
            return;
        }
        buffer.ensureCapacityToWrite(length);
        for (int i = 0, count = value.length(); i < count; ++i)
        {
            char c = value.charAt(i);

            if ((c & 0xff80) == 0)
            {
                buffer.put((byte)c);
            }
            else if ((c & 0xf800) != 0)
            {
                buffer.put((byte)(0xe0 | ((c >> 12) & 0x0f)));
                buffer.put((byte)(0x80 | ((c >> 6) & 0x3f)));
                buffer.put((byte)(0x80 | ((c >> 0) & 0x3f)));
            }
            else
            {
                buffer.put((byte)(0xc0 | ((c >> 6) & 0x1f)));
                buffer.put((byte)(0x80 | ((c >> 0) & 0x3f)));
            }
        }
    }

    /** Returns the number of bytes required to encode a datetime value. */
    public static int lengthCalendar(Calendar value) {
        return 8;
    }

    /** Encodes a datetime value into the underlying buffer. */
    public void writeCalendar(Calendar value) {
        writeFixedLong(value.getTimeInMillis());
    }

    // Write/length methods for composite types
    
    public static int lengthBytes(byte[] value) {
        int count = (value == null) ? 0 : value.length;
        int length = lengthNonnegativeInt(count);
        return length + count;
    }
    
    public void writeBytes(byte[] value) {
        boolean isNull = (value == null);
        int length = isNull ? 0 : value.length;
        writeNonnegativeInt(length);
        if (!isNull) {
            for (int i = 0; i < length; ++i) {
                writeByte(value[i]);
            }
        }
    }

    /** Returns the number of bytes required to encode a cell-derived object. */
    public static <T extends Cell> int lengthCell(T value) {
        int length = (value == null) ? 0 : value.length();
        return lengthNonnegativeInt(length) + length;
    }

    /** Encodes a cell-derived object into the underlying buffer. */
    public <T extends Cell> void writeCell(T value) {
        boolean isNull = (value == null);
        int length = isNull ? 0 : value.length();
        writeNonnegativeInt(length);
        if (!isNull) {
            value.serialize(this);
        }
    }
    
    /** Returns the number of bytes required to encode an ordered list of 32-bit
     *  integer values.
     */
    public static int lengthList(ArrayList<Integer> value) {
        int count = (value == null) ? 0 : value.size();
        int length = lengthNonnegativeInt(count);
        for (int i = 0; i < count; ++i) {
            length += lengthInt(value.get(i).intValue());
        }
        return length;
    }
    
    /** Encodes an ordered list of 32-bit integer values into the underlying buffer. */
    public void writeList(ArrayList<Integer> value) {
        boolean isNull = (value == null);
        int length = isNull ? 0 : value.size();
        writeNonnegativeInt(length);
        if (!isNull) {
            for (int i = 0; i < length; ++i) {
                writeInt(value.get(i).intValue());
            }
        }
    }

    // Write/length helper methods

    public static int lengthFixedInt(int value) {
        return 4;
    }

    /** Encodes a 32-bit signed integer by fixed-width big-endian byte order. */
    public void writeFixedInt(int value) {
        buffer.ensureCapacityToWrite(4);
        buffer.put((byte)(value >> 24));
        buffer.put((byte)(value >> 16));
        buffer.put((byte)(value >> 8));
        buffer.put((byte)value);
    }

    public static int lengthFixedLong(long value) {
        return 8;
    }

    /** Encodes a 64-bit signed integer by fixed-width big-endian byte order. */
    public void writeFixedLong(long value) {
        buffer.ensureCapacityToWrite(8);
        buffer.put((byte)(value >> 56));
        buffer.put((byte)(value >> 48));
        buffer.put((byte)(value >> 40));
        buffer.put((byte)(value >> 32));
        buffer.put((byte)(value >> 24));
        buffer.put((byte)(value >> 16));
        buffer.put((byte)(value >> 8));
        buffer.put((byte)value);
    }

    public static int lengthVariableInt(int value) {
        return lengthVariableUInt((value << 1) ^ (value >> 31));
    }

    /** Encodes a 32-bit signed integer into the underlying buffer. */
    public int writeVariableInt(int value) {
        // Zigzag encoding
        int u = (value << 1) ^ (value >> 31);
        return writeVariableUInt(u);
    }

    public static int lengthVariableLong(long value) {
        return lengthVariableULong((value << 1) ^ (value >> 63));
    }

    /** Encodes a 64-bit signed integer into the underlying buffer. */
    public int writeVariableLong(long value) {
        // Zigzag encoding
        long u = (value << 1) ^ (value >> 63);
        return writeVariableULong(u);
    }

    public static int lengthVariableUInt(int value) {
        if ((value & 0xffffff80) == 0) { return 1; }
        if ((value & 0xffffc000) == 0) { return 2; }
        if ((value & 0xffe00000) == 0) { return 3; }
        if ((value & 0xf0000000) == 0) { return 4; }
        return 5;
    }

    /** Encodes a 32-bit unsigned integer into the underlying buffer. */
    public int writeVariableUInt(int value) {
        // Unsigned LEB128 encoding
        int length = 0;
        do {
            buffer.ensureCapacityToWrite(1);
            byte b = (byte)(value & 0x7f);
            value >>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buffer.put(b);
            ++length;
        } while (value != 0);
        return length;
    }

    public static int writeVariableUInt(byte[] buffer, int value) {
        int i = 0;
        do {
            byte b = (byte)(value & 0x7f);
            value >>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buffer[i++] = b;
        } while (value != 0);
        return i;
    }

    public static int lengthVariableULong(long value) {
        if ((value & 0xffffffffffffff80L) == 0) { return 1; }
        if ((value & 0xffffffffffffc000L) == 0) { return 2; }
        if ((value & 0xffffffffffe00000L) == 0) { return 3; }
        if ((value & 0xfffffffff0000000L) == 0) { return 4; }
        if ((value & 0xfffffff800000000L) == 0) { return 5; }
        if ((value & 0xfffffc0000000000L) == 0) { return 6; }
        if ((value & 0xfffe000000000000L) == 0) { return 7; }
        if ((value & 0xff00000000000000L) == 0) { return 8; }
        if ((value & 0x8000000000000000L) == 0) { return 9; }
        return 10;
    }

    /** Encodes a 64-bit unsigned integer into the underlying buffer. */
    public int writeVariableULong(long value) {
        // Unsigned LEB128 encoding
        int length = 0;
        do {
            buffer.ensureCapacityToWrite(1);
            byte b = (byte)(value & 0x7f);
            value >>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buffer.put(b);
            ++length;
        } while (value != 0);
        return length;
    }
}
