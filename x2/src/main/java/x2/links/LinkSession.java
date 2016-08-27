// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links;

import x2.*;

public abstract class LinkSession {
    protected int handle;
    protected SessionBasedLink link;

    public void close() {
    }

    /** Sends out the specified event through this link session. */
    public void send(Event e) {
    }
}
