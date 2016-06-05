// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

/** Represents a finite set of application logic. */
public abstract class Case extends EventSink implements Setupable {
    /** Initializes this case with the specified holding flow. */
    public void setup(Flow holder) {
        flow(holder);
        
        Flow backup = Flow.current();
        Flow.current(holder);
        
        setup();
        
        Flow.current(backup);
    }
    
    /** Cleans up this case with the specified holding flow. */
    public void teardown(Flow holder) {
        Flow backup = Flow.current();
        Flow.current(holder);
        
        teardown();
        
        Flow.current(backup);
        
        close();
    }
    
    /** Initializes this case on startup. */
    protected void setup() { }
    
    /** Cleans up this case on shutdown. */
    protected void teardown() { }
}
