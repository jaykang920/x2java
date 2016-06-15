// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.io.IOException;

import x2.util.*;

/** Common base class for all events. */
public class Event extends Cell {
    protected static final Tag tag = new Tag(null, Event.class, 0, 0);

    private int handle;

    /** Constructs a new event object. */
    public Event() {
        super(tag.getNumProps());
    }

    /** Constructs a new event object with the specified fingerprint length. */
    protected Event(int length) {
        super(length + tag.getNumProps());
    }

    /** Creates a new Event object. */
    public static Event create() {
        return new Event();
    }

    /** Overridden by subclasses to build a toString chain. */
    @Override
    protected void describe(StringBuilder stringBuilder) {
        stringBuilder
            .append(' ')
            .append(_getTypeId());
    }

    /** Overridden by subclasses to build an equality test chain. */
    @Override
    protected boolean equalsTo(Cell other) {
        if (!super.equalsTo(other)) {
            return false;
        }
        Event o = (Event)other;
        if (handle != o.handle) {
            return false;
        }
        return true;
    }

    /** Returns the hash code for the current object. */
    @Override
    public int hashCode() {
        return hashCode(fingerprint, _getTypeId());
    }

    /** Returns the hash code for this event based on the specified fingerprint,
     *  assuming the given type identifier.
     */
    public int hashCode(Fingerprint fingerprint, int typeId) {
        int result = hashCode(fingerprint);
        result = Hash.update(result, -1);
        result = Hash.update(result, typeId);
        return result;
    }

    /** Overridden by subclasses to build a hash code generator chain. */
    @Override
    public int hashCode(Fingerprint fingerprint) {
        int result = super.hashCode(fingerprint);
        Capo touched = new Capo(fingerprint, tag.getOffset());
        if (touched.get(0)) {
            result = Hash.update(result, tag.getOffset() + 0);
            result = Hash.update(result, handle);
        }
        return result;
    }

    @Override
    protected boolean isEquivalent(Cell other, Fingerprint fingerprint) {
        if (!super.isEquivalent(other, fingerprint)) {
            return false;
        }
        Event o = (Event)other;
        Capo touched = new Capo(fingerprint, tag.getOffset());
        if (touched.get(0)) {
            if (handle != o.handle) {
                return false;
            }
        }
        return true;
    }

    public final void post() {
        Hub.post(this);
    }

    // Serialization

    @Override
    public void deserialize(Deserializer deserializer) throws IOException {
        super.deserialize(deserializer);
    }

    @Override
    public int length() {
        int length = Serializer.lengthInt(_getTypeId());
        length += super.length();
        return length;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.writeInt(_getTypeId());
        super.serialize(serializer);
    }

    // Built-in accessors and mutators

    /** Gets the link session handle associated with this event. */
    public int _getHandle() { return handle; }
    /** Sets the link session handle associated with this event. */
    public void _setHandle(int value) { handle = value; }

    /** Returns the type identifier of this event. */
    public int _getTypeId() { return tag.getTypeId(); }
    /** Returns the custom type tag of this event. */
    @Override
    public Cell.Tag _getTypeTag() { return tag; }

    /** Supports light-weight custom type hierarchy for Event and its subclasses. */
    public static class Tag extends Cell.Tag {
        private int typeId;

        /** Constructs a new tag object. */
        public Tag(Tag base, Class<?> runtimeType, int numProps, int typeId) {
            super(base, runtimeType, numProps);
            this.typeId = typeId;
        }

        /** Gets the type identifier of this event type. */
        public int getTypeId() { return typeId; }
    }

    public static class Equivalent extends Event {
        private Event innerEvent;
        private int innerTypeId;

        @Override
        public boolean equals(Object obj) {
            return ((Cell)obj).isEquivalent(innerEvent, fingerprint);
        }

        /*
        @Override
        protected boolean equalsTo(Cell other) {
        }
        */

        /** Returns the hash code for the current object. */
        @Override
        public int hashCode() {
            return innerEvent.hashCode(fingerprint, innerTypeId);
        }

        public Event innerEvent() {
            return innerEvent;
        }

        public void innerEvent(Event value) {
            innerEvent = value;
        }

        public int innerTypeId() {
            return innerTypeId;
        }

        public void innerTypeId(int value) {
            innerTypeId = value;
        }
    }
}
