// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.util;

import java.util.*;

/** Utility class for hash code generation. */
public class Hash {
    /** Default hash seed value. */
    public static final int SEED = 17;

    private int code;

    /** Constructs a hash object with the default seed value. */
    public Hash() {
        code = SEED;
    }

    /** Constructs a hash object with the specified seed value. */
    public Hash(int seed) {
        code = seed;
    }

    // Static update methods

    public static int update(int seed, boolean value) {
        return ((seed << 5) + seed) ^ (value ? 2 : 1);
    }

    public static int update(int seed, int value) {
        return ((seed << 5) + seed) ^ value;
    }

    public static int update(int seed, long value) {
        return ((seed << 5) + seed) ^ (int)(value ^ (value >> 32));
    }

    public static int update(int seed, float value) {
        return update(seed, (double)value);
    }

    public static int update(int seed, double value) {
        long bits = Double.doubleToRawLongBits(value);
        return update(seed, bits);
    }

    public static <T> int update(int seed, T[] value) {
        int result = seed;
        for (int i = 0, count = value.length; i < count; ++i) {
            result = update(result, value[i]);
        }
        return result;
    }

    public static <T> int update(int seed, List<T> value) {
        int result = seed;
        for (int i = 0, count = value.size(); i < count; ++i) {
            result = update(result, value.get(i));
        }
        return result;
    }

    public static <K, V> int update(int seed, Map<K, V> value) {
        int result = seed;
        for (Map.Entry<K, V> entry : value.entrySet()) {
            result = update(result, entry.getKey());
            result = update(result, entry.getValue());
        }
        return result;
    }

    public static <T extends Object> int update(int seed, T value) {
        return ((seed << 5) + seed) ^ (value != null ? value.hashCode() : 0);
    }

    /** Returns the hash code value in this instance. */
    public int code() { return code; }

    // Instance update methods

    public void update(boolean value) {
        code = update(code, value);
    }

    public void update(int value) {
        code = update(code, value);
    }

    public void update(long value) {
        code = update(code, value);
    }

    public void update(float value) {
        code = update(code, value);
    }

    public void update(double value) {
        code = update(code, value);
    }

    public <T> void update(T[] value) {
        code = update(code, value);
    }

    public <T> void update(List<T> value) {
        code = update(code, value);
    }

    public <K, V> void update(Map<K, V> value) {
        code = update(code, value);
    }

    public <T extends Object> void update(T value) {
        code = update(code, value);
    }
}
