// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.lang.ref.*;
import java.util.*;

/** Cleanup helper base class for any event-consuming classes. */
public abstract class EventSink {
    protected volatile boolean closed;
    
    private ArrayList<Binder.Token> bindings;
    private WeakReference<Flow> flow;
    
    public EventSink() {
        this(Flow.current());
    }
    
    public EventSink(Flow flow) {
        bindings = new ArrayList<Binder.Token>();
        this.flow = new WeakReference<Flow>(flow);
    }
    
    public void close() {
        if (closed) { return; }
        
        try {
            Flow flow = this.flow.get();
            if (flow == null) { return; }
            
            synchronized (bindings) {
                for (int i = 0, count = bindings.size(); i < count; ++i) {
                    flow.unsubscribe(bindings.get(i));
                }
                
                bindings.clear();
                
                this.flow = null;
            }
        }
        catch (Exception e) {
            // log error
        }
        finally {
            closed = true;
        }
    }
    
    /** Gets the flow which this EventSink belongs to. */
    public Flow flow() {
        return flow.get();
    }
    
    /** Sets the flow which this EventSink belongs to. */
    public void flow(Flow value) {
        if (bindings.size() != 0) {
            throw new IllegalStateException();
        }
        ensureNotClosed();
        flow = new WeakReference<Flow>(value);
    }
   
    protected void ensureNotClosed() {
        if (closed) {
            throw new IllegalStateException();
        }
    }
    
    public Binder.Token bind(Event e, Handler handler) {
        Flow flow = this.flow.get();
        if (flow != null) {
            Binder.Token token = flow.subscribe(e, handler);
            addBinding(token);
        }
        return null;
    }
    
    public void unbind(Binder.Token token) {
        Flow flow = this.flow.get();
        if (flow != null) {
            flow.unsubscribe(token);
            removeBinding(token);
        }
    }
    
    private void addBinding(Binder.Token token) {
        synchronized (bindings) {
            bindings.add(token);
        }
    }
    
    private void removeBinding(Binder.Token token) {
        synchronized (bindings) {
            bindings.remove(token);
        }
    }
}
