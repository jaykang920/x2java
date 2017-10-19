// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

/** This interface should be implemented by any class whose resources are to be
 *  released on cleanup. */
public interface Disposable {
    /** Releases the resources held by this object. */
    void dispose();
}
