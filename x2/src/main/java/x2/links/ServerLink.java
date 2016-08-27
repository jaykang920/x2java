// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links;

import java.util.*;
import java.util.concurrent.locks.*;

import x2.*;

/** Common base class for multi-session server links. */
public abstract class ServerLink extends SessionBasedLink {
    protected HashMap<Integer, LinkSession> sessions;

    protected ServerLink(String name) {
        super(name);
        sessions = new HashMap<Integer, LinkSession>();
    }

    /** Broadcasts the specified event to all the connected clients. */
    public void broadcast(Event e) {
        Lock rlock = rwlock.readLock();
        rlock.lock();
        ArrayList<LinkSession> snapshot;
        try {
            snapshot = new ArrayList<LinkSession>(sessions.values());
        }
        finally {
            rlock.unlock();
        }
        for (int i = 0, count = snapshot.size(); i < count; ++i) {
            snapshot.get(i).send(e);
        }
    }

    @Override
    public void close() {
        if (closed) { return; }

        // Close all the active sessions
        Lock rlock = rwlock.readLock();
        rlock.lock();
        ArrayList<LinkSession> snapshot;
        try {
            snapshot = new ArrayList<LinkSession>(sessions.values());
        }
        finally {
            rlock.unlock();
        }
        for (int i = 0, count = snapshot.size(); i < count; ++i) {
            snapshot.get(i).close();
        }

        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            sessions.clear();
        }
        finally {
            wlock.unlock();
        }

        super.close();
    }

    /** Sends out the specified event through this link channel. */
    public void send(Event e) {
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            LinkSession session = sessions.get(e._getHandle());
            if (session != null) {
                session.send(e);
            }
        }
        finally {
            rlock.unlock();
        }
    }
}
