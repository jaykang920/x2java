// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

import java.util.*;

public abstract class Link extends Case {
    private static HashSet<String> names;

    private String name;

    static {
        names = new HashSet<String>();
    }

    protected Link(String name) {
        synchronized (names) {
            if (names.contains(name)) {
                throw new IllegalArgumentException();
            }

            this.name = name;
            names.add(name);
        }
    }

    /** Closes this link and releases all the associated resources. */
    @Override
    public void close() {
        if (closed) { return; }

        synchronized (names) {
            names.remove(name);
        }
        
        super.close();
    }

    /** Gets the name of this link. */
    public String name() {
        return name;
    }

    /** Sends out the specified event through this link channel. */
    public abstract void send(Event e);

    @Override
    protected void teardown() {
        close();
    }
}
