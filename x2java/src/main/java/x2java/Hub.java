// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

import java.util.*;
import java.util.concurrent.locks.*;

/** Represents the singleton event distribution bus. */
public final class Hub {
    // List of all the flows attached to this hub
    private ArrayList<Flow> flows;
    
    private ReadWriteLock rwlock;
    
    private static Hub instance;
    
    static {
        // Initialize the singleton instance.
        instance = new Hub();
    }
    
    // Private constructor to prevent explicit instantiation
    private Hub() {
        flows = new ArrayList<Flow>();
        
        rwlock = new ReentrantReadWriteLock();
    }
    
    /** Attaches the specified flow to the hub. */
    public Hub attach(Flow flow) {
        if (flow == null) {
            throw new IllegalArgumentException();
        }
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            if (!flows.contains(flow)) {
                flows.add(flow);
            }
        }
        finally {
            wlock.unlock();
        }
        return this;
    }
    
    /** Detaches the specified flow from the hub. */
    public Hub detach(Flow flow) {
        if (flow == null) {
            throw new IllegalArgumentException();
        }
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            flows.remove(flow);
        }
        finally {
            wlock.unlock();
        }
        return this;
    }
    
    /** Detaches all the attached flows. */
    public void detachAll() {
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            flows.clear();
        }
        finally {
            wlock.unlock();
        }
    }
    
    private void feed(Event e) {
        if (e == null) {
            throw new IllegalArgumentException();
        }
        
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            for (int i = 0, count = flows.size(); i < count; ++i) {
                flows.get(i).feed(e);
            }
        }
        finally {
            rlock.unlock();
        }
    }
    
    /** Gets the singleton instance of the hub. */
    public static Hub instance() {
        return instance;
    }
    
    /** Posts up the specified event to the hub. */
    public static void post(Event e) {
        instance().feed(e);
    }
    
    private void startFlows() {
        ArrayList<Flow> snapshot;
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            snapshot = new ArrayList<Flow>(flows);
        }
        finally {
            rlock.unlock();
        }
        for (int i = 0, count = snapshot.size(); i < count; ++i) {
            snapshot.get(i).startup();
        }
    }
    
    /** Starts all the flows attached to the hub. */
    public static void startup() {
        instance().startFlows();
    }
    
    /** Stops all the flows attached to the hub. */
    public static void shutdown() {
        try {
            instance().stopFlows();
        }
        catch (Exception e) {
            // nop
        }
    }
    
    private void stopFlows() {
        ArrayList<Flow> snapshot;
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            snapshot = new ArrayList<Flow>(flows);
        }
        finally {
            rlock.unlock();
        }
        for (int i = snapshot.size() - 1; i >= 0; --i) {
            try {
                snapshot.get(i).shutdown();
            }
            catch (Exception e) {
                // log error
            }
        }
    }
}
