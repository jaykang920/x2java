// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.util;

import x2java.*;

import x2java.util.*;

/** Provides an offset-based read-only window on a fingerprint. */
public class Capo {
    private Fingerprint fingerprint;
    private int offset;

    /** Constructs a new capo object based on the specified fingerprint and
     *  offset.
     */
    public Capo(Fingerprint fingerprint, int offset) {
        this.fingerprint = fingerprint;
        this.offset = offset;
    }

    /** Gets the bit value at the actual index of (offset + index). */
    public boolean get(int index) {
        int effectiveIndex = offset + index;
        if (effectiveIndex < 0 || fingerprint.getLength() <= effectiveIndex) {
            return false;
        }
        return fingerprint.get(effectiveIndex);
    }
}
