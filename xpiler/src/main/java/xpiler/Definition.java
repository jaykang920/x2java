// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.util.*;

/** Represents an abstract definition. */
abstract class Definition {
    public String name;

    public abstract void format(FormatterContext context);
}

/** Represents a set of constants. */
class ConstsDef extends Definition {
    /** Represents a constant definition. */
    public static class Constant {
        public String name;
        public String value;

        public String comment;
    }

    public String type;
    public String nativeType;

    public String comment;

    private List<Constant> constants = new Vector<Constant>();

    @Override
    public void format(FormatterContext context) {
        context.formatConsts(this);
    }

    public List<Constant> getConstants() { return constants; }

    public boolean hasConstants() { return !constants.isEmpty(); }
}

/** Represents a cell definition. */
class CellDef extends Definition {
    /** Represents a cell property. */
    public static class Property {
        public int index;
        public String name;
        public String type;
        public String defaultValue;
        public String nativeName;
        public String nativeType;

        public String comment;
    }

    public String base;
    public String baseClass;
    public boolean isLocal;

    public String comment;

    private List<Property> properties = new Vector<Property>();

    @Override
    public void format(FormatterContext context) {
        context.formatCell(this);
    }

    public List<Property> getProperties() { return properties; }
  
    public boolean hasProperties() { return !properties.isEmpty(); }

    public boolean isEvent() { return false; }
}

/** Represents an event definition. */
class EventDef extends CellDef {
    public String id;

    @Override
    public boolean isEvent() { return true; }
}

/** Represents a reference definition. */
class Reference {
    public String target;

    public void format(FormatterContext context) {
        context.formatReference(this);
    }
}
