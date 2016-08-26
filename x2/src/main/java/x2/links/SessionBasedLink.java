// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links;

import java.util.concurrent.locks.*;

import x2.*;

/**  Abstract base class for session-based links. */
public abstract class SessionBasedLink extends Link {
    private ReadWriteLock rwlock;

    static {
        // event factory registration here
    }

    protected SessionBasedLink(String name) {
        super(name);

        rwlock = new ReentrantReadWriteLock();
    }

    /** Called when a new session creation attempt is completed. */
    protected void onSessionConnected(boolean result, Object context) {
    }

    /** Called when an existing link session is closed. */
    protected void onSessionDisconnected(int handle, Object context) {
    }

    /** Initializes this link on startup. */
    @Override
    protected void setup() {
        bind(new LinkSessionConnected().setLinkName(name()), new Handler() {
            public void invoke(Event e) {
                onLinkSessionConnected((LinkSessionConnected)e);
            }
        });
        bind(new LinkSessionDisconnected().setLinkName(name()), new Handler() {
            public void invoke(Event e) {
                onLinkSessionDisconnected((LinkSessionDisconnected)e);
            }
        });
    }

    // LinkSessionConnected event handler
    private void onLinkSessionConnected(LinkSessionConnected e) {
        onSessionConnected(e.getResult(), e.getContext());
    }

    // LinkSessionDisconnected event handler
    private void onLinkSessionDisconnected(LinkSessionDisconnected e) {
        onSessionDisconnected(e.getHandle(), e.getContext());
    }
}
