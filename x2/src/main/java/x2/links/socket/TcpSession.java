// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2.links.socket;

import java.nio.channels.*;

import x2.links.*;
import x2.util.*;

public class TcpSession extends LinkSession {
    protected SocketChannel channel;

    public TcpSession(SessionBasedLink link, SocketChannel channel) {
        super(link);
        this.channel = channel;
    }

    /** Returns the underlying socket channel object. */
    public SocketChannel channel() {
        return channel;
    }

    @Override
    public void close() {
        if (closed) { return; }

        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                // log
            }
        }

        super.close();
    }
}
