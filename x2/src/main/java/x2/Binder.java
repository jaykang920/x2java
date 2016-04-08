// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/** Manages event-handler bindings. */
public class Binder {
    private HashMap<Event, HandlerSet> handlerMap;
    private Filter filter;

    private ReadWriteLock rwlock;

    public Binder() {
        handlerMap = new HashMap<Event, HandlerSet>();
        filter = new Filter();

        rwlock = new ReentrantReadWriteLock();
    }

    public void bind(Token token) {
        bind(token.getKey(), token.getValue());
    }

    public Token bind(Event e, Handler handler) {
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            HandlerSet handlerSet = handlerMap.get(e);
            if (handlerSet == null) {
                handlerSet = new HandlerSet();
                handlerMap.put(e, handlerSet);
            }

            if (handlerSet.add(handler)) {
                filter.add(e._getTypeId(), e._getFingerprint());
            }

            return new Token(e, handler);
        }
        finally {
            wlock.unlock();
        }
    }

    // buildHandlerChain

    public void unbind(Token token) {
        unbind(token.getKey(), token.getValue());
    }

    public Binder.Token unbind(Event e, Handler handler) {
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            HandlerSet handlers = handlerMap.get(e);
            if (handlers == null) {
                return null;
            }
            if (!handlers.remove(handler)) {
                return null;
            }
            if (handlers.size() == 0) {
                handlerMap.remove(e);
            }
            filter.remove(e._getTypeId(), e._getFingerprint());

            return new Token(e, handler);
        }
        finally {
            wlock.unlock();
        }
    }

    // Static nested classes

    public static class Token {
        private final Event key;
        private final Handler value;

        public Token(Event key, Handler value) {
            this.key = key;
            this.value = value;
        }

        public Event getKey() { return key; }
        public Handler getValue() { return value; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Token)) {
                return false;
            }
            Token other = (Token)o;
            return (key.equals(other.key) && value.equals(other.value));
        }

        @Override
        public int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }
    }

    private static class Filter {
        private Map<Integer, List<Slot>> map;

        public Filter() {
            map = new HashMap<Integer, List<Slot>>();
        }

        public void add(int typeId, Fingerprint fingerprint) {
            List<Slot> slots;
            slots = map.get(typeId);
            if (slots == null) {
                slots = new ArrayList<Slot>();
                map.put(typeId, slots);
            }
            Slot slot = new Slot(fingerprint);
            int index = Collections.binarySearch(slots, slot);
            if (index >= 0) {
                slots.get(index).addRef();
            }
            else {
                index = ~index;
                slots.add(index, slot);
            }
        }

        public List<Slot> get(int typeId) {
            return map.get(typeId);
        }

        public void remove(int typeId, Fingerprint fingerprint) {
            List<Slot> slots = map.get(typeId);
            if (slots == null) {
                return;
            }
            int index = Collections.binarySearch(slots, new Slot(fingerprint));
            if (index >= 0) {
                if (slots.get(index).removeRef() == 0) {
                    slots.remove(index);
                }
                if (slots.size() == 0) {
                    map.remove(typeId);
                }
            }

        }
    }

    private static class HandlerSet {
        private List<Handler> list;

        public HandlerSet() {
            list = new ArrayList<Handler>();
        }

        public boolean add(Handler handler) {
            if (list.contains(handler)) {
                return false;
            }
            list.add(handler);
            return true;
        }

        public List<Handler> getList() { return list; }

        public boolean remove(Handler handler) {
            return list.remove(handler);
        }

        public int size() {
            return list.size();
        }
    }

    // Extends Fingerprint class to hold an additional reference count.
    private static class Slot extends Fingerprint {
        private AtomicInteger refCount;

        /** Constructs a new slot object that contains the bit values copied from
         *  the specified fingerprint.
         *  @param fingerprint  a fingerprint object to copy from.
         */
        public Slot(Fingerprint fingerprint) {
            super(fingerprint);
            refCount = new AtomicInteger(1);
        }

        /** Increases the reference count of this slot.
         *  @returns  the resultant reference count.
         */
        public int addRef() {
            return refCount.incrementAndGet();
        }

        /** Decreases the reference count of this slot.
         *  @returns  the resultant reference count.
         */
        public int removeRef() {
            return refCount.decrementAndGet();
        }
    }
}
