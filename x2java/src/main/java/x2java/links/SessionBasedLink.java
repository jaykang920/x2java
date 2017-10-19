// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.links;

import java.util.concurrent.locks.*;

import x2java.*;

/** Abstract base class for session-based links. */
public abstract class SessionBasedLink extends Link {
    protected ReadWriteLock rwlock;

    static {
        // event factory registration here
    }

    protected SessionBasedLink(String name) {
        super(name);

        rwlock = new ReentrantReadWriteLock();
    }

    protected void onLinkSessionConnectedInternal(boolean result, Object context) {
        if (result) {
            // handle
            // connected
        }

        onSessionConnectedInternal(result, context);

        Hub.post(new LinkSessionConnected()
            .setLinkName(name())
            .setResult(result)
            .setContext(context));
    }

    /** Called when a new session creation attempt is completed. */
    protected void onSessionConnected(boolean result, Object context) {
    }

    /** Called when an existing link session is closed. */
    protected void onSessionDisconnected(int handle, Object context) {
    }

    protected abstract void onSessionConnectedInternal(boolean result, Object context);

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
