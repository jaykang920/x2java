// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.flows;

import x2.*;

/**  */
public abstract class EventBasedFlow extends Flow {
    protected BlockingQueue<Event> queue;
    protected final Object syncRoot = new Object();
    
    protected EventBasedFlow() {
        queue = new BlockingQueue<Event>();
    }
    
    public void feed(Event e) {
        queue.enqueue(e);
    }
}
