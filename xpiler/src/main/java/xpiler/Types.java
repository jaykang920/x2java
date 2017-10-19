// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.util.*;

class TypeSpec {
    public String type;
    public List<TypeSpec> details;

    public TypeSpec(String type, List<TypeSpec> details) {
        this.type = type;
        this.details = details;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(type);
        if (details != null && details.size() != 0) {
            sb.append('(');
            for (int i = 0; i < details.size(); ++i) {
                if (i != 0) { sb.append(", "); }
                sb.append(details.get(i).toString());
            }
            sb.append(')');
        }
        return sb.toString();
    }
}

class TypeProperty {
    public boolean isPrimitive;
    public boolean isCollection;
    public boolean detailRequired;

    public TypeProperty(
            boolean isPrimitive, boolean isCollection, boolean detailRequired) {
        this.isPrimitive = isPrimitive;
        this.isCollection = isCollection;
        this.detailRequired = detailRequired;
    }
}

class Types {
    private static HashMap<String, TypeProperty> types;

    static {
        types = new HashMap<String, TypeProperty>();

        // Primitive types
        types.put("bool", new TypeProperty(true, false, false));
        types.put("byte", new TypeProperty(true, false, false));
        types.put("int8", new TypeProperty(true, false, false));
        types.put("int16", new TypeProperty(true, false, false));
        types.put("int32", new TypeProperty(true, false, false));
        types.put("int64", new TypeProperty(true, false, false));
        types.put("float32", new TypeProperty(true, false, false));
        types.put("float64", new TypeProperty(true, false, false));
        types.put("string", new TypeProperty(true, false, false));
        types.put("datetime", new TypeProperty(true, false, false));
        types.put("bytes", new TypeProperty(true, false, false));

        // Collection types
        types.put("list", new TypeProperty(false, true, true));
        types.put("map", new TypeProperty(false, true, true));
    }

    private Types() { }

    public static boolean isBuiltin(String type) {
        return types.containsKey(type);
    }

    public static boolean isCollection(String type) {
        TypeProperty typeProperty = types.get(type);
        return (typeProperty != null ? typeProperty.isCollection : false);
    }

    public static boolean isPrimitive(String type) {
        TypeProperty typeProperty = types.get(type);
        return (typeProperty != null ? typeProperty.isPrimitive : false);
    }

    public static TypeSpec parse(String s) {
        Context context = new Context();
        return parseTypeSpec(s, context);
    }

    private static TypeSpec parseTypeSpec(String s, Context context) {
        String type = null;
        List<TypeSpec> details = null;

        int backMargin = 0;
        int start = context.index;
        for (; context.index < s.length(); ++context.index) {
            char c = s.charAt(context.index);
            if (c == '(' && context.index < (s.length() - 1)) {
                type = s.substring(start, context.index).trim();
                ++context.index;
                details = parseDetails(s, context);
                backMargin = 1;
                break;
            }
            else if (c == ',') {
                ++context.index;
                backMargin = 1;
                break;
            }
            else if (c == ')') {
                break;
            }
        }
        if (type == null) {
            type = s.substring(start, context.index - backMargin).trim();
        }
        return (type.length() == 0 ? null : new TypeSpec(type, details));
    }

    private static List<TypeSpec> parseDetails(String s, Context context) {
        List<TypeSpec> details = new Vector<TypeSpec>();

        for (; context.index < s.length(); ++context.index) {
            char c = s.charAt(context.index);
            if (c == ',') {
                continue;
            }
            if (c == ')') {
                ++context.index;
                break;
            }
            else {
                TypeSpec detail = parseTypeSpec(s, context);
                if (detail != null) {
                    details.add(detail);
                    --context.index;
                }
            }
        }
        return (details.size() == 0 ? null : details);
    }

    private static class Context {
        public int index;
    }
}
