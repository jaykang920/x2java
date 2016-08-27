// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links;

import java.util.concurrent.locks.*;

import x2.*;

/** Common base class for single-session client links. */
public abstract class ClientLink extends SessionBasedLink {
    protected LinkSession session;  // current link session

    protected ClientLink(String name) {
        super(name);
    }

    @Override
    public void close() {
        if (closed) { return; }

        LinkSession session = null;
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            if (this.session != null) {
                session = this.session;
                this.session = null;
            }
        }
        finally {
            wlock.unlock();
        }

        if (session != null) {
            session.close();
        }

        super.close();
    }

    @Override
    public void send(Event e) {
        LinkSession currentSession = session();
        if (currentSession == null) {
            // drop event
        }
        currentSession.send(e);
    }

    public LinkSession session() {
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            return session;
        }
        finally {
            rlock.unlock();
        }
    }
}
