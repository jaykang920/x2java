// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links;

import x2.*;

import x2.util.*;

public abstract class LinkSession {
    protected int handle;
    protected SessionBasedLink link;
    protected volatile boolean closed;

    Buffer rxBuffer;

    protected LinkSession(SessionBasedLink link) {
        this.link = link;

        rxBuffer = new Buffer();
    }

    public void close() {
        if (closed) { return; }

        closed = true;

        // buffer clearing
    }

    /** Sends out the specified event through this link session. */
    public void send(Event e) {
        if (closed) {
            return;
        }

        synchronized {
            eventsToSend.add(e);

            if (txFlag || closed) {
                return;
            }

            txFlag = true;
        }

        sendInternal();
    }

    protected void onReceiveInternal(int bytesTransferred) {
        Log.trace("%s %d received %d bytes", link.name(), handle, bytesTransferred);

        if (closed) {
            return;
        }

        rxBuffer.stretch(bytesTransferred);
    }
}
