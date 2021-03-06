// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

/** The Handler interface should be implemented by any class whose instances are
 *  intended to be executed by a flow to handle an event. */
public interface Handler {
    void invoke(Event e);
}
