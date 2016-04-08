// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package x2;

import java.lang.reflect.*;
import java.util.*;

/** Holds a map of retrievable events and their runtime types. */
public final class EventFactory {
    private static Map<Integer, Class<?>> map;

    static {
        map = new HashMap<Integer, Class<?>>();
    }

    // Private constructor to prohibit explicit instantiation.
    private EventFactory() { }

    /** Creates a new event instance of the specified type identifier. */
    public static Event create(int typeId) {
        Class<?> cls = map.get(typeId);
        if (cls == null) {
            // error
            return null;
        }
        try {
            return (Event)cls.newInstance();
        }
        catch (Exception e) {
            // error
            return null;
        }
    }

    /** Registers the specified runtime type as a retrievable event. */
    public static void register(Class<?> cls) {
        try {
            Field field = cls.getField("TypeId");
            int typeId = field.getInt(null);
            register(typeId, cls);
        }
        catch (Exception e) {
            // error
        }
    }

    /** Registers a retrievable event type identifier with its runtime type. */
    public static void register(int typeId, Class<?> cls) {
        Class<?> existing = map.get(typeId);
        if (existing != null && !existing.equals(cls)) {
            throw new IllegalArgumentException();
        }
        map.put(typeId, cls);
    }
}