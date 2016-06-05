// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

/** Represents a logically independent execution flow. */
public abstract class Flow {
    protected static ThreadLocal<Flow> current;
    
    protected Binder binder;
    protected CaseStack caseStack;
    protected String name;
    
    protected Flow() {
        binder = new Binder();
        caseStack = new CaseStack();
        name = getClass().getName();
    }
    
    /** Adds the specified case to this flow. */
    public Flow add(Setupable o) {
        caseStack.add(o);
        return this;
    }
    
    public static Binder.Token bind(Event e, Handler handler) {
        return current().subscribe(e, handler);
    }
    
    /** Gets the current flow in which the current thread is running. */
    public static Flow current() {
        return current.get();
    }
    
    /** Sets the current flow in which the current thread is running. */
    public static void current(Flow value) {
        current.set(value);
    }
    
    public abstract void feed(Event e);
    
    /** Gets the name of this flow. */
    public String name() {
        return name;
    }
    
    /** Removes the specified case from this flow. */
    public Flow remove(Setupable o) {
        caseStack.remove(o);
        return this;
    }

    public abstract void shutdown();
    
    public abstract Flow startup();
    
    public Binder.Token subscribe(Event e, Handler handler) {
        return binder.bind(e, handler);
    }
    
    public static void unbind(Binder.Token token) {
        current().unbind(token);
    }
    
    public void unsubscribe(Binder.Token token) {
        binder.unbind(token);
    } 
}
