// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java.links;

import java.nio.ByteBuffer;
import java.util.*;

import x2java.*;

import x2java.util.*;

public abstract class LinkSession {
    protected int handle;
    protected SessionBasedLink link;
    protected volatile boolean closed;

    protected Buffer rxBuffer;
    protected List<ByteBuffer> rxBufferList;
    protected List<ByteBuffer> txBufferList;

    protected List<Event> eventsSending;
    protected List<Event> eventsToSend;

    protected int lengthToReceive;

    protected boolean txFlag;

    protected final Object syncRoot = new Object();

    protected LinkSession(SessionBasedLink link) {
        this.link = link;

        rxBuffer = new Buffer();

        rxBufferList = new ArrayList<ByteBuffer>();
        txBufferList = new ArrayList<ByteBuffer>();

        eventsSending = new ArrayList<Event>();
        eventsToSend = new ArrayList<Event>();
    }

    public int handle() {
        return handle;
    }

    public void close() {
        if (closed) { return; }

        closed = true;

        // buffer clearing
    }

    /** Sends out the specified event through this link session. */
    public void send(Event e) {
        Log.debug("%s send()", link.name());

        if (closed) {
            return;
        }

        synchronized (syncRoot) {
            eventsToSend.add(e);

            if (txFlag || closed) {
                return;
            }

            txFlag = true;
        }

        beginSend();
    }

    protected void onReceiveInternal(int bytesTransferred) {
        Log.trace("%s %d received %d bytes", link.name(), handle, bytesTransferred);

        if (closed) {
            return;
        }

        rxBuffer.stretch(bytesTransferred);

        //
        rxBuffer.rewind();
        parseHeader();  // xxx
        //

        //
        rxBuffer.markToRead(lengthToReceive);

        Deserializer deserializer = new Deserializer(rxBuffer);
        Event retrieved = deserializer.create();
        if (retrieved == null) {
            return;
        }

        try {
            retrieved.deserialize(deserializer);
        } catch (Exception e) {
            Log.error("%s error loading event", link.name());
            return;
        }

        retrieved._setHandle(handle);

        Log.debug("%s retrieved event %s", link.name(), retrieved.toString());

        Hub.post(retrieved);
    }

    protected abstract void buildHeader(SendBuffer sendBuffer);
    protected abstract boolean parseHeader();

    protected abstract void sendInternal();

    private void beginSend() {
        Log.debug("%s beginSend()", link.name());

        synchronized (syncRoot) {
            if (eventsToSend.size() == 0) {
                return;
            }
            // Swap send buffers
            if (eventsSending.size() != 0) {
                eventsSending.clear();
            }
            List<Event> temp = eventsSending;
            eventsSending = eventsToSend;
            eventsToSend = temp;
            temp = null;
        }

        // Capture send buffers.
        txBufferList.clear();
        int lengthToSend = 0;
        int count = eventsSending.size();
        
        for (int i = 0; i < count; ++i) {
            Event e = eventsSending.get(i);

            SendBuffer sendBuffer = new SendBuffer();
            e.serialize(new Serializer(sendBuffer.getBuffer()));
            //sendBuffer.getBuffer().flip();

            buildHeader(sendBuffer);

            sendBuffer.listOccupiedBuffers(txBufferList);
        }

        sendInternal();
    }
}
