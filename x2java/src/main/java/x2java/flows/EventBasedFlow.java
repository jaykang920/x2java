// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.flows;

import x2java.*;

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
