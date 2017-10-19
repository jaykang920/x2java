// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.util;

/** Provides a mutable wrapper class. */
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
