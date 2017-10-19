// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.links;

import java.util.concurrent.locks.*;

import x2java.*;
import x2java.util.*;

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

    protected void onConnectInternal(LinkSession linkSession) {
        //linkSession.polatiry(true);
        onLinkSessionConnectedInternal(true, linkSession);
    }

    @Override
    protected void onSessionConnectedInternal(boolean result, Object context) {
        if (result) {
            Lock wlock = rwlock.writeLock();
            wlock.lock();
            try {
                session = (LinkSession)context;
            }
            finally {
                wlock.unlock();
            }
        }
    }

    @Override
    public void send(Event e) {
        LinkSession currentSession = session();
        if (currentSession == null) {
            Log.warn("%s dropped event %s", name(), e.toString());
            return;
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

