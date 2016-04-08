// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.io.IOException;

/** Common base class for all custom types. */
public abstract class Cell {
    /** Per-class type tag to support custom type hierarchy. */
    protected static final Tag tag = new Tag(null, Cell.class, 0);

    /** Fingerprint to keep track of property assignments. */
    protected Fingerprint fingerprint;

    /** Constructs a new cell object with the specified fingerprint length. */
    protected Cell(int length) {
        fingerprint = new Fingerprint(length);
    }

    /** Determines whether the specified object is equal to this one. */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Cell)) {
            return false;
        }
        Cell other = (Cell) obj;
        return other.equalsTo(this);
    }

    /** Overridden by subclasses to build an equality test chain. */
    protected boolean equalsTo(Cell other) {
        if (getClass() != other.getClass()) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether the specified cell object is equivalent to this one.
     */
    public boolean equivalent(Cell other) {
        return equivalent(other, fingerprint);
    }

    /**
     * Determines whether the specified cell object is equivalent to this one
     * based on the given fingerprint.
     */
    public boolean equivalent(Cell other, Fingerprint fingerprint) {
        if (!other.isKindOf(this)) {
            return false;
        }
        if (!fingerprint.equivalent(other.fingerprint)) {
            return false;
        }
        return isEquivalent(other, fingerprint);
    }

    /** Returns the hash code for the current object. */
    @Override
    public int hashCode() {
        return hashCode(fingerprint);
    }

    /** Overridden by subclasses to build a hash code generator chain. */
    public int hashCode(Fingerprint fingerprint) {
        return x2.util.Hash.SEED;
    }

    /** Overridden by subclasses to build an equivalence test chain. */
    protected boolean isEquivalent(Cell other, Fingerprint fingerprint) {
        return true;
    }

    /**
     * Determines whether this cell object is a kind of the specified cell in
     * the custom type hierarchy.
     */
    public boolean isKindOf(Cell other) {
        Tag tag = _getTypeTag();
        Tag otherTag = other._getTypeTag();
        while (tag != null) {
            if (tag == otherTag) {
                return true;
            }
            tag = tag.getBase();
        }
        return false;
    }

    /** Returns a string that describes the current object. */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(_getTypeTag().getRuntimeType().getName()).append(" {");
        describe(stringBuilder);
        stringBuilder.append(" }");
        return stringBuilder.toString();
    }

    /** Overridden by subclasses to build a toString chain. */
    protected void describe(StringBuilder stringBuilder) {
    }

    // Serialization

    /** Overridden by subclasses to build a deserialization chain. */
    public void deserialize(Deserializer deserializer) throws IOException {
        fingerprint.deserialize(deserializer);
    }

    /** Overridden by subclasses to build an encoded length computation chain. */
    public int length() {
        return fingerprint.length();
    }

    /** Overridden by subclasses to build a serialization chain. */
    public void serialize(Serializer serializer) {
        fingerprint.serialize(serializer);
    }

    // Built-in accessors and mutators

    /** Gets the fingerprint of this cell. */
    public Fingerprint _getFingerprint() {
        return fingerprint;
    }

    /** Sets the fingerprint of this cell as the specified one. */
    void _setFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    /** Gets the custom type tag of this object. */
    public Tag _getTypeTag() {
        return tag;
    }

    /**
     * Supports light-weight custom type hierarchy for Cell and its subclasses.
     */
    public static class Tag {
        private Tag base;
        private Class<?> runtimeType;
        private int numProps;
        private int offset;

        /** Constructs a new tag object. */
        public Tag(Tag base, Class<?> runtimeType, int numProps) {
            this.base = base;
            this.runtimeType = runtimeType;
            this.numProps = numProps;
            if (base != null) {
                offset = base.getOffset() + base.getNumProps();
            }
        }

        /** Gets the immediate base type tag. */
        public Tag getBase() {
            return base;
        }

        /** Gets the correspondent runtime type. */
        public Class<?> getRuntimeType() {
            return runtimeType;
        }

        /**
         * Gets the number of immediate (directly defined) properties in this
         * type.
         */
        public int getNumProps() {
            return numProps;
        }

        /**
         * Gets the fingerprint offset for immediate properties in this type.
         */
        public int getOffset() {
            return offset;
        }
    }
}
