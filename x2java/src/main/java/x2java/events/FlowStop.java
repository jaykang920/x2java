// auto-generated by x2java xpiler

package x2java;

import java.io.IOException;
import java.util.*;

import x2java.*;
import x2java.util.*;

public class FlowStop extends Event {
    protected static Tag tag;

    static {
        tag = new Tag(Event.tag, FlowStop.class, 0, BuiltinEventType.FlowStop);
    }

    public FlowStop() {
        super(tag.getNumProps());
    }

    protected FlowStop(int length) {
        super(length + tag.getNumProps());
    }

    @Override
    protected boolean equalsTo(Cell other) {
        if (!super.equalsTo(other)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(Fingerprint fingerprint) {
        return super.hashCode(fingerprint);
    }

    @Override
    protected boolean isEquivalent(Cell other, Fingerprint fingerprint) {
        if (!super.isEquivalent(other, fingerprint)) {
            return false;
        }
        return true;
    }

    @Override
    public Cell.Tag _getTypeTag() { return tag; }

    @Override
    public int _getTypeId() { return tag.getTypeId(); }

    @Override
    protected void describe(StringBuilder sb) {
        super.describe(sb);
    }
}