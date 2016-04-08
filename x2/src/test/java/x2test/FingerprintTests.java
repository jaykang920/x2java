package x2test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import x2.Fingerprint;

public class FingerprintTests extends TestCase {
    public FingerprintTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(FingerprintTests.class);
    }

    public void testNegativeLength() {
        try {
            new Fingerprint(-1);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAccessors() {
        Fingerprint fp = new Fingerprint(33);

        try {
            fp.get(-1);
            fail("expected IndexOfOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            fp.get(33);
            fail("expected IndexOfOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        assertFalse(fp.get(31));
        fp.touch(31);;
        assertTrue(fp.get(31));
        fp.wipe(31);;
        assertFalse(fp.get(31));

        assertFalse(fp.get(32));
        fp.touch(32);;
        assertTrue(fp.get(32));
        fp.wipe(32);;
        assertFalse(fp.get(32));
    }

    public void testCreation() {
        Fingerprint fp1 = new Fingerprint(1);
        assertEquals(1, fp1.length());
        assertFalse(fp1.get(0));

        Fingerprint fp2 = new Fingerprint(33);
        assertEquals(33, fp2.length());
        for (int i = 0; i < 33; ++i)
        {
            assertFalse(fp2.get(i));
        }
    }

    public void testCopyCreation() {
        Fingerprint fp1 = new Fingerprint(65);
        fp1.touch(32);
        Fingerprint fp2 = new Fingerprint(fp1);
        assertTrue(fp2.get(32));

        // Ensure that the original block array is not shared
        fp1.touch(64);
        assertFalse(fp2.get(64));
    }

    public void testClear() {
        // length > 32
        Fingerprint fp = new Fingerprint(65);
        for (int i = 0; i < 65; ++i)
        {
            fp.touch(i);
            assertTrue(fp.get(i));
        }
        fp.clear();
        for (int i = 0; i < 65; ++i)
        {
            assertFalse(fp.get(i));
        }

        // length <= 32
        Fingerprint fp2 = new Fingerprint(6);
        for (int i = 0; i < 6; ++i)
        {
            fp2.touch(i);
            assertTrue(fp2.get(i));
        }
        fp.clear();
        for (int i = 0; i < 6; ++i)
        {
            assertFalse(fp.get(i));
        }
    }

    public void testComparison() {
        Fingerprint fp1 = new Fingerprint(65);
        Fingerprint fp2 = new Fingerprint(65);
        Fingerprint fp3 = new Fingerprint(64);
        Fingerprint fp4 = new Fingerprint(66);

        // Length first

        assertTrue(fp3.compareTo(fp1) < 0);
        assertTrue(fp1.compareTo(fp3) > 0);
        fp3.touch(2);
        assertTrue(fp3.compareTo(fp1) < 0);
        assertTrue(fp1.compareTo(fp3) > 0);

        assertTrue(fp4.compareTo(fp2) > 0);
        assertTrue(fp2.compareTo(fp4) < 0);
        fp2.touch(64);
        assertTrue(fp4.compareTo(fp2) > 0);
        assertTrue(fp2.compareTo(fp4) < 0);
        fp2.wipe(64);

        // Bits second
        assertEquals(0, fp1.compareTo(fp2));

        fp1.touch(31);
        assertTrue(fp1.compareTo(fp2) > 0);
        assertTrue(fp2.compareTo(fp1) < 0);

        fp2.touch(32);
        assertTrue(fp1.compareTo(fp2) < 0);
        assertTrue(fp2.compareTo(fp1) > 0);

        fp1.touch(32);
        assertTrue(fp1.compareTo(fp2) > 0);
        assertTrue(fp2.compareTo(fp1) < 0);

        fp2.touch(31);
        assertEquals(0, fp1.compareTo(fp2));
        assertEquals(0, fp2.compareTo(fp1));

        fp2.touch(64);
        assertTrue(fp1.compareTo(fp2) < 0);
        assertTrue(fp2.compareTo(fp1) > 0);
    }

    public void testEquality() {
        Fingerprint fp1 = new Fingerprint(65);
        Fingerprint fp2 = new Fingerprint(65);
        Fingerprint fp3 = new Fingerprint(64);
        Fingerprint fp4 = new Fingerprint(66);

        // Reference first
        assertTrue(fp1.equals(fp1));
        assertTrue(fp2.equals(fp2));
        assertTrue(fp3.equals(fp3));
        assertTrue(fp4.equals(fp4));

        // Type second
        assertFalse(fp1.equals(new Object()));

        // Length third

        assertFalse(fp3.equals(fp1));
        assertFalse(fp1.equals(fp3));

        assertFalse(fp4.equals(fp2));
        assertFalse(fp2.equals(fp4));

        // Bits forth

        assertTrue(fp1.equals(fp2));
        assertTrue(fp2.equals(fp1));

        fp1.touch(32);
        assertFalse(fp1.equals(fp2));
        assertFalse(fp2.equals(fp1));

        fp2.touch(32);
        assertTrue(fp1.equals(fp2));
        assertTrue(fp2.equals(fp1));

        // Length <= 32
        Fingerprint fp5 = new Fingerprint(7);
        Fingerprint fp6 = new Fingerprint(7);
        fp5.touch(0);
        assertFalse(fp5.equals(fp6));
        fp6.touch(0);
        assertTrue(fp5.equals(fp6));
    }

    public void testHashing() {
        Fingerprint fp1 = new Fingerprint(65);
        Fingerprint fp2 = new Fingerprint(65);
        Fingerprint fp3 = new Fingerprint(64);
        Fingerprint fp4 = new Fingerprint(66);

        assertEquals(fp1.hashCode(), fp2.hashCode());
        assertTrue(fp1.hashCode() != fp3.hashCode());
        assertTrue(fp2.hashCode() != fp4.hashCode());

        fp1.touch(32);
        assertTrue(fp1.hashCode() != fp2.hashCode());
        fp2.touch(32);
        assertEquals(fp1.hashCode(), fp2.hashCode());
    }

    public void testEquivalence()
    {
        Fingerprint fp1 = new Fingerprint(65);
        Fingerprint fp2 = new Fingerprint(65);
        Fingerprint fp3 = new Fingerprint(64);
        Fingerprint fp4 = new Fingerprint(66);

        // Reference first
        assertTrue(fp1.equivalent(fp1));
        assertTrue(fp2.equivalent(fp2));
        assertTrue(fp3.equivalent(fp3));
        assertTrue(fp4.equivalent(fp4));

        // Length second

        assertTrue(fp3.equivalent(fp1));
        assertFalse(fp1.equivalent(fp3));

        assertFalse(fp4.equivalent(fp2));
        assertTrue(fp2.equivalent(fp4));

        // Bits third

        assertTrue(fp1.equivalent(fp2));
        assertTrue(fp2.equivalent(fp1));

        fp1.touch(32);
        assertFalse(fp1.equivalent(fp2));
        assertTrue(fp2.equivalent(fp1));

        fp2.touch(32);
        assertTrue(fp1.equivalent(fp2));
        assertTrue(fp2.equivalent(fp1));

        fp2.touch(31);
        assertTrue(fp1.equivalent(fp2));
        assertFalse(fp2.equivalent(fp1));

        fp4.touch(31);
        fp4.touch(32);
        fp4.touch(33);
        assertTrue(fp2.equivalent(fp4));
        assertFalse(fp4.equivalent(fp2));
    }
}
