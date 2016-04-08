// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.io.IOException;
import java.util.Calendar;

import x2.util.*;

/** Binary wire format deserializer. */
public final class Deserializer {
    private Buffer buffer;
    private int marker;

    /** Constructs a new deserializer object that works on the specified buffer. */
    public Deserializer(Buffer buffer) {
        this.buffer = buffer;
        marker = -1;
    }

    /** Creates a new event instance, retrieving the type identifier from this
     *  deserializer.
     */
    public Event create() {
        int typeId;
        try {
            typeId = readInt();
        }
        catch (Exception e) {
            // error
            return null;
        }
        return EventFactory.create(typeId);
    }

    /** Loads a new cell of type T from this deserializer. */
    @SuppressWarnings("unchecked")
    private <T extends Cell> T load(Class<T> cls) {
        T value = null;
        if (Event.class.isAssignableFrom(cls)) {
            value = (T)create();
        }
        else {
            try {
                value = cls.newInstance();
            }
            catch (Exception e) {
                // error
            }
        }
        if (value != null) {
            try {
                value.deserialize(this);
            }
            catch (IOException ioe) {
                // error
                value = null;
            }
        }
        return value;
    }


    // Read methods for primitive types

    /** Decodes a boolean value out of the underlying buffer. */
    public boolean readBoolean() throws IOException {
        buffer.checkLengthToRead(1);
        return (buffer.get() != 0);
    }

    /** Decodes a single byte out of the underlying buffer. */
    public byte readByte() throws IOException {
        buffer.checkLengthToRead(1);
        return buffer.get();
    }

    /** Decodes a 16-bit signed integer out of the underlying buffer. */
    public short readShort() throws IOException {
        buffer.checkLengthToRead(2);
        short value = buffer.get();
        value = (short)((value << 8) | buffer.get());
        return value;
    }

    /** Decodes a 32-bit signed integer out of the underlying buffer. */
    public int readInt() throws IOException {
        return readVariableInt(null);
    }

    /** Decodes a 32-bit non-negative integer out of the underlying buffer. */
    public int readNonnegativeInt() throws IOException {
        int value = readVariableUInt(null);
        if (value < 0) {
            throw new IOException();
        }
        return value;
    }

    /** Decodes a 64-bit signed integer out of the underlying buffer. */
    public long readLong() throws IOException {
        return readVariableLong(null);
    }

    /** Decodes a 32-bit floating-point number out of the underlying buffer. */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readFixedInt());
    }

    /** Decodes a 64-bit floating-point number out of the underlying buffer. */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readFixedLong());
    }

    /** Decodes a text string out of the underlying buffer. */
    public String readString() throws IOException {
        // UTF-8 decoding
        int length = readNonnegativeInt();
        if (length == 0) {
            return new String();
        }
        buffer.checkLengthToRead(length);
        char c, c2, c3;
        int bytesRead = 0;
        StringBuilder stringBuilder = new StringBuilder(length);
        while (bytesRead < length) {
            c = (char)buffer.get();
            switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                ++bytesRead;
                stringBuilder.append(c);
                break;
            case 12: case 13:
                // 110x xxxx  10xx xxxx
                bytesRead += 2;
                if (bytesRead > length) {
                    throw new IOException();
                }
                c2 = (char)buffer.get();
                if ((c2 & 0xc0) != 0x80) {
                    throw new IOException();
                }
                stringBuilder.append((char)(((c & 0x1f) << 6) | (c2 & 0x3f)));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                bytesRead += 3;
                if (bytesRead > length) {
                    throw new IOException();
                }
                c2 = (char)buffer.get();
                c3 = (char)buffer.get();
                if (((c2 & 0xc0) != 0x80) || ((c3 & 0xc0) != 0x80))
                {
                    throw new IOException();
                }
                stringBuilder.append((char)(((c & 0x0f) << 12) |
                  ((c2 & 0x3f) << 6) | ((c3 & 0x3f) << 0)));
                break;
            default:
                // 10xx xxxx  1111 xxxx
                throw new IOException();
            }
        }
        return stringBuilder.toString();
    }

    /** Decodes a datetime value out of the underlying buffer. */
    public Calendar readCallendar() throws IOException {
        Calendar value = Calendar.getInstance();
        value.setTimeInMillis(readFixedLong());
        return value;
    }

    // Read methods for composite types

    /** Decodes a cell-derived object out of the underlying buffer. */
    public <T extends Cell> T readCell(Class<T> cls) throws IOException {
        T value = null;
        int length = readNonnegativeInt();
        if (length == 0) { return value; }

        int markerSaved = marker;
        marker = buffer.position() + length;

        value = load(cls);

        if (buffer.position() != marker)
        {
            buffer.setPosition(marker);
        }
        marker = markerSaved;
        return value;
    }

    // Read helper methods

    /** Decodes a 32-bit signed integer by fixed-width big-endian byte order. */
    public int readFixedInt() throws IOException {
        buffer.checkLengthToRead(4);
        int value = buffer.get();
        value |= buffer.get() << 8;
        value |= buffer.get() << 16;
        value |= buffer.get() << 24;
        return value;
    }

    /** Decodes a 64-bit signed integer by fixed-width big-endian byte order. */
    public long readFixedLong() throws IOException {
        buffer.checkLengthToRead(8);
        long value = buffer.get();
        value |= (long)buffer.get() << 8;
        value |= (long)buffer.get() << 16;
        value |= (long)buffer.get() << 24;
        value |= (long)buffer.get() << 32;
        value |= (long)buffer.get() << 40;
        value |= (long)buffer.get() << 48;
        value |= (long)buffer.get() << 56;
        return value;
    }

    /** Decodes a 32-bit signed integer out of the underlying buffer. */
    public int readVariableInt(Mutable<Integer> length) throws IOException {
        // Zigzag decoding
        int u = readVariableUInt(length);
        return ((u >> 1) & 0x7fffffff) ^ -(u & 1);
    }

    /** Decodes a 64-bit signed integer out of the underlying buffer. */
    public long readVariableLong(Mutable<Integer> length) throws IOException {
        // Zigzag decoding
        long u = readVariableULong(length);
        return ((u >> 1) & 0x7fffffffffffffffL) ^ -(u & 1);
    }

    /** Decodes a 32-bit unsigned integer out of the underlying buffer. */
    public int readVariableUInt(Mutable<Integer> length) throws IOException {
        // Unsigned LEB128 decoding
        int value = 0;
        int i, shift = 0;
        for (i = 0; i < 5; ++i) {
            buffer.checkLengthToRead(1);
            int b = buffer.get();
            value |= ((b & 0x7f) << shift);
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        if (length != null) {
            if (i < 5) {
                length.set(Integer.valueOf(i + 1));
            }
            else {
                // error
                length.set(Integer.valueOf(0));
            }
        }
        return value;
    }

    /** Decodes a 64-bit unsigned integer out of the underlying buffer. */
    public long readVariableULong(Mutable<Integer> length) throws IOException {
        // Unsigned LEB128 decoding
        long value = 0L;
        int i, shift = 0;
        for (i = 0; i < 10; ++i) {
            buffer.checkLengthToRead(1);
            int b = buffer.get();
            value |= ((long)(b & 0x7f) << shift);
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        if (length != null) {
            if (i < 10) {
                length.set(Integer.valueOf(i + 1));
            }
            else {
                // error
                length.set(Integer.valueOf(0));
            }
        }
        return value;
    }
}
