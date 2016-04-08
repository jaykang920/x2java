// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.util;

/** Provides an offset-based read-only window on a fingerprint. */
public class Mutable<T> {
    private T value;

    public Mutable() {
    }

    public Mutable(T initialValue) {
        value = initialValue;
    }

    public T get() { return value; }
    public void set(T value) { this.value = value; }
}
