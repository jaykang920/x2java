// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

/** Defines methods to initialize/finalize a case. */
public interface Setupable {
    void setup(Flow holder);
    void teardown(Flow holder);
}
