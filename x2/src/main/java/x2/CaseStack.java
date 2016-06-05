// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.util.*;

/** Represents a finite set of application logic. */
public class CaseStack implements Setupable {
    private ArrayList<Setupable> cases;
    private boolean activated;
    
    public CaseStack() {
        cases = new ArrayList<Setupable>();
    }
    
    public void add(Setupable o) {
        synchronized (cases) {
            cases.add(o);
        }
    }
    
    public void remove(Setupable o) {
        synchronized (cases) {
            cases.remove(o);
        }
    }
    
    public void setup(Flow holder) {
        ArrayList<Setupable> snapshot;
        synchronized (cases) {
            if (activated) { return; }
            activated = true;
            snapshot = new ArrayList<Setupable>(cases);
        }
        for (int i = 0, count = snapshot.size(); i < count; ++i) {
            snapshot.get(i).setup(holder);
        }
    }
    
    public void teardown(Flow holder) {
        ArrayList<Setupable> snapshot;
        synchronized (cases) {
            if (!activated) { return; }
            activated = false;
            snapshot = new ArrayList<Setupable>(cases);
        }
        for (int i = snapshot.size() - 1; i >= 0; --i) {
            try {
                snapshot.get(i).teardown(holder);
            }
            catch (Exception e) {
                // TODO: log error
            }
        }
    }
}
