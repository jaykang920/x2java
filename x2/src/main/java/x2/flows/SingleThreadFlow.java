// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.flows;

import java.util.*;

import x2.*;

/** Represents a finite set of application logic. */
public class SingleThreadFlow extends EventBasedFlow implements Runnable {
    protected Thread thread;
    
    public SingleThreadFlow() {
    }
    
    public SingleThreadFlow(String name) {
        this.name = name;
    }
    
    public Flow startup() {
        synchronized (syncRoot) {
            if (thread == null) {
                setup();
                caseStack.setup(this);
                thread = new Thread(this);
                thread.start();
                // enqueue FlowStart
            }
        }
        return this;
    }
    
    public void shutdown() {
        synchronized (syncRoot) {
            if (thread == null) {
                return;
            }
            // enquque FlowStop and quit queue
            try {
                thread.join();
            }
            catch (InterruptedException ie) { }
            thread = null;
            
            caseStack.teardown(this);
            teardown();
        }
    }
    
    public void run() {
        current.set(this);
        
        equivalent.set(new Event.Equivalent());
        events.set(new ArrayList<Event>());
        handlerChain.set(new ArrayList<Handler>());
        
        List<Event> dequeued = events.get();
        
        while (true) {
            if (queue.dequeue(dequeued) == 0) {
                break;
            }
            for (int i = 0, count = dequeued.size(); i < count; ++i) {
                dispatch(dequeued.get(i));
            }
            dequeued.clear();
        }
        
        handlerChain.set(null);
        events.set(null);
        equivalent.set(null);
        
        current.set(null);
    }
}
