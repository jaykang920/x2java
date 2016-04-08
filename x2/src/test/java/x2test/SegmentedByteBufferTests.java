package x2test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.nio.ByteBuffer;

import x2.util.SegmentedByteBuffer;

public class SegmentedByteBufferTests extends TestCase {
    public SegmentedByteBufferTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SegmentedByteBufferTests.class);
    }

    public void testSegmentedByteBuffer() {
        SegmentedByteBuffer segmented1 = new SegmentedByteBuffer(2, 1);
        SegmentedByteBuffer segmented2 = new SegmentedByteBuffer(1, 1);

        ByteBuffer b11 = segmented1.acquire();
        assertNotNull(b11);
        assertEquals(0, b11.position());
        assertEquals(1, b11.limit());
        assertEquals(1, b11.capacity());
        ByteBuffer b12 = segmented1.acquire();
        assertNotNull(b12);
        assertEquals(0, b12.position());
        assertEquals(1, b12.limit());
        assertEquals(1, b12.capacity());
        ByteBuffer b13 = segmented1.acquire();
        assertNull(b13);

        // Should be independent.
        b11.put((byte)1);
        b12.put((byte)2);
        b11.flip();
        byte b = b11.get();
        assertEquals((byte)1, b);

        ByteBuffer b21 = segmented2.acquire();
        assertNotNull(b21);
        ByteBuffer b22 = segmented2.acquire();
        assertNull(b22);

        assertTrue(b11.hasArray());
        assertTrue(b12.hasArray());
        assertEquals(b11.array(), b12.array());

        assertTrue(b21.hasArray());
        assertFalse(b11.array() == b21.array());

        assertTrue(segmented1.release(b11));
        assertTrue(segmented1.release(b12));
        assertFalse(segmented1.release(b21));
        assertTrue(segmented2.release(b21));
    }
}
