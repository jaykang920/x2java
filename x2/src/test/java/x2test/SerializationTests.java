package x2test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

import x2.*;
import x2.util.*;

public class SerializationTests extends TestCase {
    public SerializationTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SerializationTests.class);
    }

    public void testVarInt32() throws IOException {
        Buffer buffer = new Buffer();
        Serializer serializer = new Serializer(buffer);
        Deserializer deserializer = new Deserializer(buffer);

        serializer.writeInt(0);
        serializer.writeInt(-1);
        serializer.writeInt(1);
        serializer.writeInt(Integer.MAX_VALUE);
        serializer.writeInt(Integer.MIN_VALUE);

        buffer.rewind();

        Mutable<Integer> bytes = new Mutable<Integer>();
        int i = deserializer.readVariableInt(bytes);
        assertEquals(0, i);
        assertEquals(1, bytes.get().intValue());

        i = deserializer.readVariableInt(bytes);
        assertEquals(-1, i);
        assertEquals(1, bytes.get().intValue());

        i = deserializer.readVariableInt(bytes);
        assertEquals(1, i);
        assertEquals(1, bytes.get().intValue());

        i = deserializer.readVariableInt(bytes);
        assertEquals(Integer.MAX_VALUE, i);
        assertEquals(5, bytes.get().intValue());

        i = deserializer.readVariableInt(bytes);
        assertEquals(Integer.MIN_VALUE, i);
        assertEquals(5, bytes.get().intValue());

        buffer.trim();

        serializer.writeInt(0x0000ef80 >> 1);  // 2
        serializer.writeInt(0x001fc000 >> 1);  // 3
        serializer.writeInt(0x0fe00000 >> 1);  // 4

        buffer.rewind();

        i = deserializer.readVariableInt(bytes);
        assertEquals(0x0000ef80 >> 1, i);
        assertEquals(2, bytes.get().intValue());

        i = deserializer.readVariableInt(bytes);
        assertEquals(0x001fc000 >> 1, i);
        assertEquals(3, bytes.get().intValue());

        i = deserializer.readVariableInt(bytes);
        assertEquals(0x0fe00000 >> 1, i);
        assertEquals(4, bytes.get().intValue());
    }

    public void testVarInt64() throws IOException {
        Buffer buffer = new Buffer();
        Serializer serializer = new Serializer(buffer);
        Deserializer deserializer = new Deserializer(buffer);

        serializer.writeLong(0L);
        serializer.writeLong(-1L);
        serializer.writeLong(1L);
        serializer.writeLong(Long.MAX_VALUE);
        serializer.writeLong(Long.MIN_VALUE);

        buffer.rewind();

        Mutable<Integer> bytes = new Mutable<Integer>();
        long l = deserializer.readVariableLong(bytes);
        assertEquals(0, l);
        assertEquals(1, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(-1, l);
        assertEquals(1, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(1, l);
        assertEquals(1, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(Long.MAX_VALUE, l);
        assertEquals(10, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(Long.MIN_VALUE, l);
        assertEquals(10, bytes.get().intValue());

        buffer.trim();

        serializer.writeLong(0x00003f80L >> 1);  // 2
        serializer.writeLong(0x001fc000L >> 1);  // 3
        serializer.writeLong(0x0fe00000L >> 1);  // 4
        serializer.writeLong(0x00000007f0000000L >> 1);  // 5
        serializer.writeLong(0x000003f800000000L >> 1);  // 6
        serializer.writeLong(0x0001fc0000000000L >> 1);  // 7
        serializer.writeLong(0x00fe000000000000L >> 1);  // 8
        serializer.writeLong(0x7f00000000000000L >> 1);  // 9

        buffer.rewind();

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x00003f80L >> 1, l);
        assertEquals(2, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x001fc000L >> 1, l);
        assertEquals(3, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x0fe00000L >> 1, l);
        assertEquals(4, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x00000007f0000000L >> 1, l);
        assertEquals(5, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x000003f800000000L >> 1, l);
        assertEquals(6, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x0001fc0000000000L >> 1, l);
        assertEquals(7, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x00fe000000000000L >> 1, l);
        assertEquals(8, bytes.get().intValue());

        l = deserializer.readVariableLong(bytes);
        assertEquals(0x7f00000000000000L >> 1, l);
        assertEquals(9, bytes.get().intValue());
    }

    public void testFixedInt32() throws IOException {
        Buffer buffer = new Buffer();
        Serializer serializer = new Serializer(buffer);
        Deserializer deserializer = new Deserializer(buffer);

        serializer.writeFixedInt(0);
        serializer.writeFixedInt(-1);
        serializer.writeFixedInt(1);
        serializer.writeFixedInt(Integer.MAX_VALUE);
        serializer.writeFixedInt(Integer.MIN_VALUE);

        buffer.rewind();

        int i = deserializer.readFixedInt();
        assertEquals(0, i);

        i = deserializer.readFixedInt();
        assertEquals(-1, i);

        i = deserializer.readFixedInt();
        assertEquals(1, i);

        i = deserializer.readFixedInt();
        assertEquals(Integer.MAX_VALUE, i);

        i = deserializer.readFixedInt();
        assertEquals(Integer.MIN_VALUE, i);
    }

    public void testFixedInt64() throws IOException {
        Buffer buffer = new Buffer();
        Serializer serializer = new Serializer(buffer);
        Deserializer deserializer = new Deserializer(buffer);

        serializer.writeFixedLong(0L);
        serializer.writeFixedLong(-1L);
        serializer.writeFixedLong(1L);
        serializer.writeFixedLong(Long.MAX_VALUE);
        serializer.writeFixedLong(Long.MIN_VALUE);

        buffer.rewind();

        long l = deserializer.readFixedLong();
        assertEquals(0, l);

        l = deserializer.readFixedLong();
        assertEquals(-1, l);

        l = deserializer.readFixedLong();
        assertEquals(1, l);

        l = deserializer.readFixedLong();
        assertEquals(Long.MAX_VALUE, l);

        l = deserializer.readFixedLong();
        assertEquals(Long.MIN_VALUE, l);
    }

    public void testFloat32() throws IOException {
        Buffer buffer = new Buffer();
        Serializer serializer = new Serializer(buffer);
        Deserializer deserializer = new Deserializer(buffer);

        // Boundary value tests

        serializer.writeFloat(0.0F);
        serializer.writeFloat(Float.MIN_VALUE);
        serializer.writeFloat(Float.MAX_VALUE);
        serializer.writeFloat(Float.NEGATIVE_INFINITY);
        serializer.writeFloat(Float.POSITIVE_INFINITY);
        serializer.writeFloat(Float.NaN);

        buffer.rewind();

        float f = deserializer.readFloat();
        assertEquals(0.0F, f);
        f = deserializer.readFloat();
        assertEquals(Float.MIN_VALUE, f);
        f = deserializer.readFloat();
        assertEquals(Float.MAX_VALUE, f);
        f = deserializer.readFloat();
        assertEquals(Float.NEGATIVE_INFINITY, f);
        f = deserializer.readFloat();
        assertEquals(Float.POSITIVE_INFINITY, f);
        f = deserializer.readFloat();
        assertEquals(Float.NaN, f);

        buffer.trim();

        // Intermediate value tests

        serializer.writeFloat(0.001234F);
        serializer.writeFloat(8765.4321F);

        buffer.rewind();

        f = deserializer.readFloat();
        assertEquals(0.001234F, f);
        f = deserializer.readFloat();
        assertEquals(8765.4321F, f);
    }

    public void testFloat64() throws IOException {
        Buffer buffer = new Buffer();
        Serializer serializer = new Serializer(buffer);
        Deserializer deserializer = new Deserializer(buffer);

        // Boundary value tests

        serializer.writeDouble(0.0);
        serializer.writeDouble(Double.MIN_VALUE);
        serializer.writeDouble(Double.MAX_VALUE);
        serializer.writeDouble(Double.NEGATIVE_INFINITY);
        serializer.writeDouble(Double.POSITIVE_INFINITY);
        serializer.writeDouble(Double.NaN);

        buffer.rewind();

        double d = deserializer.readDouble();
        assertEquals(0.0, d);
        d = deserializer.readDouble();
        assertEquals(Double.MIN_VALUE, d);
        d = deserializer.readDouble();
        assertEquals(Double.MAX_VALUE, d);
        d = deserializer.readDouble();
        assertEquals(Double.NEGATIVE_INFINITY, d);
        d = deserializer.readDouble();
        assertEquals(Double.POSITIVE_INFINITY, d);
        d = deserializer.readDouble();
        assertEquals(Double.NaN, d);

        buffer.trim();

        // Intermediate value tests

        serializer.writeDouble(0.001234);
        serializer.writeDouble(8765.4321);

        buffer.rewind();

        d = deserializer.readDouble();
        assertEquals(0.001234, d);
        d = deserializer.readDouble();
        assertEquals(8765.4321, d);
    }
}
