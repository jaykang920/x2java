// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.util;

import java.util.Stack;

/** Thread-safe minimal generic object pool. */
public class Pool<T> {
    private Stack<T> store;
    private int capacity;

    /** Constructs a new pool object without a capacity limit. */
    public Pool() {
        this(0);
    }

    /** Constructs a new pool object with the specified maximum capacity. */
    public Pool(int capacity) {
        store = new Stack<T>();
        this.capacity = capacity;
    }

    /** Tries to pop an object out of the pool, or returns null. */
    public synchronized T pop() {
        if (store.size() != 0) {
            return store.pop();
        }
        return null;
    }

    /** Tries to push the specified object into the pool. */
    public synchronized boolean push(T item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        if (capacity != 0 && store.size() >= capacity) {
            return false;
        }
        store.push(item);
        return true;
    }
}
