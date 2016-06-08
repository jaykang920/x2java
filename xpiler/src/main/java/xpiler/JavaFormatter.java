// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;

class JavaFormatter implements Formatter {
    private static final String extension = ".java";
    private static final String description = "Java";

    public boolean format(Document doc, String outDir) {
        List<Definition> definitions = doc.getDefinitions();
        for (Definition def : definitions) {
            def.name = StringUtil.firstToUpper(def.name);
        }

        if (!Xpiler.getOptions().isForced()) {
            boolean flag = true;
            for (Definition def : definitions) {
                if (!isUpToDate(doc.inputPath, outDir, def)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return true;
            }
        }

        System.out.println(PathUtil.getFilename(doc.inputPath));

        boolean result = true;
        for (Definition def : definitions) {
            if (!format(doc, def, outDir)) {
                result = false;
            }
        }
        return result;
    }

    private boolean format(Document doc, Definition def, String outDir) {
        PrintStream out = null;
        try {
            String targetName = PathUtil.join(outDir, def.name + extension);
            File file = new File(targetName);
            out = new PrintStream(new FileOutputStream(file), true, "UTF-8");
            Context context = new Context();
            context.out = out;
            context.doc = doc;
            formatHeader(context);
            def.format(context);
        } catch (Exception e) {
            System.err.format("error: %s\n", e.getMessage());
            return false;
        } finally {
            try {
                out.close();
            } catch (Exception e) { }
        }
        return true;
    }

    private void formatHeader(Context context) {
        PrintStream o = context.out;
        o.println("// auto-generated by x2java xpiler");
        o.println();
        String namespace = context.doc.namespace;
        if (StringUtil.isNullOrEmpty(namespace) == false) {
            o.format("package %s;", namespace.replace('/', '.').toLowerCase());
            o.println();
            o.println();
        }
        o.println("import java.io.IOException;");
        o.println("import java.util.*;");
        o.println();
        o.println("import x2.*;");
        o.println("import x2.util.*;");
        o.println();
        
        if (!context.doc.getReferences().isEmpty()) {
            for (Reference reference : context.doc.getReferences()) {
                reference.format(context);
            }
            o.println();
        }
    }

    public String getDescription() { return description; }

    private boolean isUpToDate(String path, String outDir, Definition def) {
        File source = new File(path);
        File target = new File(PathUtil.join(outDir, def.name + extension));
        return (target.exists() && target.lastModified() >= source.lastModified());
    }

    private static class Context extends FormatterContext {
        private static class TypeTrait {
            public String nativeType;
            public String objectType;
            public String typeName;
            public String defaultValue;
        }
        private static Map<String, TypeTrait> typeTraits;

        private static final String tab = "    ";
        private int baseIndentation = 0;

        @Override
        public void formatReference(Reference reference) {
            out.format("import %s.*;",
                    reference.target.replace('/', '.').toLowerCase());
            out.println();;
        }

        @Override
        public void formatConsts(ConstsDef def) {
            if (typeTraits.containsKey(def.type)) {
                def.nativeType = typeTraits.get(def.type).nativeType;
            }
            else {
                return;
            }

            if (def.type == "string") {
                for (ConstsDef.Constant constant : def.getConstants())
                {
                    constant.value = "\"" + constant.value + "\"";
                }
            }

            indent(0); out.format("public final class %s {\n", def.name);
            indent();
            indent(0); out.format("private %s() { }\n", def.name);
            out.println();
            for (ConstsDef.Constant constant : def.getConstants()) {
                indent(0);
                out.format("public static final %s %s", def.nativeType, constant.name);
                if (!StringUtil.isNullOrEmpty(constant.value)) {
                    out.format(" = %s", constant.value);
                }
                out.println(';');
            }
            unindent();
            indent(0); out.println('}');
        }

        @Override
        public void formatCell(CellDef def) {
            def.baseClass = def.base;
            if (StringUtil.isNullOrEmpty(def.baseClass)) {
                def.baseClass = (def.isEvent() ? "Event" : "Cell");
            }
            indent(0); out.format("public class %s extends %s {\n",
                    def.name, def.baseClass);
            indent();
            indent(0); out.println("protected static Tag tag;");
            preprocessProperties(def);
            formatPropertyFields(def);
            formatProperties(def);
            formatMethods(def);
            unindent();
            indent(0); out.println('}');
        }

        private void formatPropertyFields(CellDef def) {
            if (def.hasProperties()) {
                out.println();
            }
            for (CellDef.Property prop : def.getProperties()) {
                indent(0); out.format("private %s %s;", prop.nativeType, prop.nativeName);
                out.println();
            }
        }

        private void formatProperties(CellDef def) {
            for (CellDef.Property prop : def.getProperties()) {
                out.println();
                indent(0); out.format("public %s get%s() {",
                        prop.nativeType, prop.name);
                out.println();
                indent(1); out.format("return %s;", prop.nativeName);
                out.println();
                indent(0); out.println("}");
                indent(0); out.format("public %s set%s(%s value) {",
                        def.name, prop.name, prop.nativeType);
                out.println();
                indent(1); out.format("%s = value;", prop.nativeName);
                out.println();
                indent(1); out.println("return this;");
                indent(0); out.println("}");
            }
        }

        private void formatMethods(CellDef def) {
            formatStaticInitializer(def);
            formatConstructor(def);
            formatInitializer(def);
            formatEqualsTo(def);
            formatHashCode(def);
            formatIsEquivalent(def);
            formatTypeAccessors(def);
            formatDescribe(def);

            out.println();
            indent(0); out.println("// Serialization");

            formatDeserialize(def);
            formatLength(def);
            formatSerialize(def);
        }
        
        private void formatStaticInitializer(CellDef def) {
            String baseTag = def.base;
            if (baseTag == null || baseTag.isEmpty()) {
                baseTag = (def.isEvent() ? "Event.tag" : "null");
            }
            else {
                baseTag += ".tag";
            }
            out.println();
            indent(0); out.println("static {");
            indent(1); out.print(String.format("tag = new Tag(%s, %s.class, %d",
                baseTag, def.name, def.getProperties().size()));
            if (def.isEvent()) {
                out.print(String.format(", %s", ((EventDef)def).id));
            }
            out.println(");");
            indent(0); out.println("}");
        }

        private void formatConstructor(CellDef def) {
            out.println();
            indent(0); out.format("public %s() {", def.name);
            out.println();
            indent(1); out.println("super(tag.getNumProps());");
            indent(1); out.println("init();");
            indent(0); out.println("}");

            out.println();
            indent(0); out.format("protected %s(int length) {", def.name);
            out.println();
            indent(1); out.println("super(length + tag.getNumProps());");
            indent(1); out.println("init();");
            indent(0); out.println("}");
        }

        private void formatInitializer(CellDef def) {
            out.println();
            indent(0); out.println("private void init() {");
            for (CellDef.Property prop : def.getProperties()) {
                indent(1); out.format("%s = %s;", prop.nativeName, prop.defaultValue);
                out.println();
            }
            indent(0); out.println("}");
        }

        private void formatEqualsTo(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("protected boolean equalsTo(Cell other) {");
            indent(1); out.println("if (!super.equalsTo(other)) {");
            indent(2); out.println("return false;");
            indent(1); out.println("}");
            if (def.hasProperties()) {
                indent(1); out.format("%s o = (%s)other;", def.name, def.name);
                out.println();
                for (CellDef.Property prop : def.getProperties()) {
                    indent(1); out.format("if (%s != o.%s) {",
                            prop.nativeName, prop.nativeName);
                    out.println();
                    indent(2); out.println("return false;");
                    indent(1); out.println("}");
                }
            }
            indent(1); out.println("return true;");
            indent(0); out.println("}");
        }

        private void formatHashCode(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("public int hashCode(Fingerprint fingerprint) {");
            if (def.hasProperties()) {
                indent(1); out.println("Hash hash = new Hash(super.hashCode(fingerprint));");
                indent(1); out.println("Capo touched = fingerprint.capo(tag.getOffset());");
                for (CellDef.Property prop : def.getProperties()) {
                    indent(1); out.format("if (touched.get(%d)) {", prop.index);
                    out.println();
                    indent(2); out.format("hash.update(%s);", prop.nativeName);
                    out.println();
                    indent(1); out.println("}");
                }
                indent(1); out.println("return hash.code();");
            } else {
                indent(1); out.println("return super.hashCode(fingerprint);");
            }
            indent(0); out.println("}");
        }

        private void formatIsEquivalent(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("protected boolean isEquivalent(Cell other, Fingerprint fingerprint) {");
            indent(1); out.println("if (!super.isEquivalent(other, fingerprint)) {");
            indent(2); out.println("return false;");
            indent(1); out.println("}");
            if (def.hasProperties()) {
                indent(1); out.format("%s o = (%s)other;", def.name, def.name);
                out.println();
                indent(1); out.println("Capo touched = fingerprint.capo(tag.getOffset());");
                for (CellDef.Property prop : def.getProperties()) {
                    indent(1); out.format("if (touched.get(%d)) {", prop.index);
                    out.println();
                    indent(2); out.format("if (%s != o.%s) {", prop.nativeName, prop.nativeName);
                    out.println();
                    indent(3); out.println("return false;");
                    indent(2); out.println("}");
                    indent(1); out.println("}");
                }
            }
            indent(1); out.println("return true;");
            indent(0); out.println("}");
        }

        private void formatTypeAccessors(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.format("public %s _getTypeTag() { return tag; }",
                (def.isEvent() ? "Cell.Tag" : "Tag"));
            out.println();
            if (def.isEvent()) {
                out.println();
                indent(0); out.println("@Override");
                indent(0); out.println("public int _getTypeId() { return tag.getTypeId(); }");
            }
        }

        private void formatDescribe(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("protected void describe(StringBuilder sb) {");
            indent(1); out.println("super.describe(sb);");
            for (CellDef.Property prop : def.getProperties()) {
                indent(1); out.print("sb.append(\"");
                if (prop.index == 0) {
                    out.print(" ");
                } else {
                    out.print(", ");
                }
                out.print(prop.name);
                out.print(": \"");
                out.println(");");
                indent(1); out.print("sb.append(");
                out.print(prop.nativeName);
                out.println(");");
            }
            indent(0); out.println("}");
        }

        private void formatDeserialize(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("public void deserialize(Deserializer deserializer) throws IOException {");
            indent(1); out.println("super.deserialize(deserializer);");
            if (def.hasProperties()) {
                indent(1); out.println("Capo touched = fingerprint.capo(tag.getOffset());");
                for (CellDef.Property prop : def.getProperties()) {
                    indent(1); out.format("if (touched.get(%d)) {", prop.index);
                    out.println();
                    indent(2); out.format("%s = deserializer.%s;", prop.nativeName,
                            formatReadMethod(prop));
                    out.println();
                    indent(1); out.println("}");
                }
            }
            indent(0); out.println("}");
        }

        private void formatLength(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("public int length() {");
            indent(1); out.println("int length = super.length();");
            if (def.hasProperties()) {
                indent(1); out.println("Capo touched = fingerprint.capo(tag.getOffset());");
                for (CellDef.Property prop : def.getProperties()) {
                    indent(1); out.format("if (touched.get(%d)) {", prop.index);
                    out.println();
                    indent(2); out.format("length += Serializer.%s(%s);",
                            formatLengthMethod(prop), prop.nativeName);
                    out.println();
                    indent(1); out.println("}");
                }
            }
            indent(1); out.println("return length;");
            indent(0); out.println("}");
        }

        private void formatSerialize(CellDef def) {
            out.println();
            indent(0); out.println("@Override");
            indent(0); out.println("public void serialize(Serializer serializer) {");
            indent(1); out.println("super.serialize(serializer);");
            if (def.hasProperties()) {
                indent(1); out.println("Capo touched = fingerprint.capo(tag.getOffset());");
                for (CellDef.Property prop : def.getProperties()) {
                    indent(1); out.format("if (touched.get(%d)) {", prop.index);
                    out.println();
                    indent(2); out.format("serializer.%s(%s);",
                            formatWriteMethod(prop), prop.nativeName);
                    out.println();
                    indent(1); out.println("}");
                }
            }
            indent(0); out.println("}");
        }

        private static void preprocessProperties(CellDef def) {
            int index = 0;
            for (CellDef.Property prop : def.getProperties()) {
                prop.index = index++;

                prop.nativeName = StringUtil.firstToLower(prop.name) + "_";
                prop.name = StringUtil.firstToUpper(prop.name);
                
                TypeTrait typeTrait = typeTraits.get(prop.typeSpec.type);
                // typeTrait != null
                prop.trait = typeTrait;

                if (Types.isPrimitive(prop.typeSpec.type)) {
                    if (StringUtil.isNullOrEmpty(prop.defaultValue)) {
                        prop.defaultValue = typeTrait.defaultValue;
                    }
                    if (prop.typeSpec.type == "string") {
                        prop.defaultValue = "\"" + prop.defaultValue + "\"";
                    }
                }
                else {
                    prop.defaultValue = "null";
                }

                prop.nativeType = formatTypeSpec(prop.typeSpec, false);
            }
        }

        private static String formatTypeSpec(TypeSpec typeSpec, boolean boxing) {
            String type = typeSpec.type;
            if (!Types.isBuiltin(type)) {
                return type;  // custom type
            }
            TypeTrait typeTrait = typeTraits.get(type);
            return Types.isPrimitive(type)
                    ? (boxing ? typeTrait.objectType : typeTrait.nativeType)
                    : formatCollectionType(typeSpec);
        }

        private static String formatCollectionType(TypeSpec typeSpec) {
            StringBuilder sb = new StringBuilder(typeTraits.get(typeSpec.type).nativeType);
            if (typeSpec.details != null) {
                sb.append('<');
                boolean leading = true;
                for (TypeSpec detail : typeSpec.details) {
                    if (leading) {
                        leading = false;
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append(formatTypeSpec(detail, true));
                }
                sb.append('>');
            }
            return sb.toString();
        }

        private static String formatReadMethod(CellDef.Property prop) {
            String type = prop.typeSpec.type;
            if (!Types.isBuiltin(type)) {
                return String.format("readCell(%s.class)", type);
            }
            if (Types.isPrimitive(type)) {
                return String.format("read%s()", ((TypeTrait)prop.trait).typeName);
            }
            else {
                return String.format("read%s(%s.class)",
                        ((TypeTrait)prop.trait).typeName, prop.nativeType);
            }
        }

        private static String formatLengthMethod(CellDef.Property prop) {
            String type = prop.typeSpec.type;
            if (!Types.isBuiltin(type)) {
                return "lengthCell";
            }
            return String.format("length%s", ((TypeTrait)prop.trait).typeName);
        }

        private static String formatWriteMethod(CellDef.Property prop) {
            String type = prop.typeSpec.type;
            if (!Types.isBuiltin(type)) {
                return "writeCell";
            }
            return String.format("write%s", ((TypeTrait)prop.trait).typeName);
        }

        public void indent() {
            ++baseIndentation;
        }

        public void unindent() {
            --baseIndentation;
        }

        private void indent(int level) {
            for (int i = 0; i < (baseIndentation + level); ++i) {
                out.print(tab);
            }
        }

        static {
            typeTraits = new HashMap<String, TypeTrait>();
            
            TypeTrait typeTrait = new TypeTrait();
            typeTrait.nativeType = "boolean";
            typeTrait.objectType = "Boolean";
            typeTrait.typeName = "Bool";
            typeTrait.defaultValue = "false";
            typeTraits.put("bool", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "byte";
            typeTrait.objectType = "Byte";
            typeTrait.typeName = "Byte";
            typeTrait.defaultValue = "0";
            typeTraits.put("byte", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "byte";
            typeTrait.objectType = "Byte";
            typeTrait.typeName = "Byte";
            typeTrait.defaultValue = "0";
            typeTraits.put("int8", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "short";
            typeTrait.objectType = "Short";
            typeTrait.typeName = "Short";
            typeTrait.defaultValue = "0";
            typeTraits.put("int16", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "int";
            typeTrait.objectType = "Integer";
            typeTrait.typeName = "Int";
            typeTrait.defaultValue = "0";
            typeTraits.put("int32", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "long";
            typeTrait.objectType = "Long";
            typeTrait.typeName = "Long";
            typeTrait.defaultValue = "0";
            typeTraits.put("int64", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "float";
            typeTrait.objectType = "Float";
            typeTrait.typeName = "Float";
            typeTrait.defaultValue = ".0f";
            typeTraits.put("float32", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "double";
            typeTrait.objectType = "Double";
            typeTrait.typeName = "Double";
            typeTrait.defaultValue = ".0";
            typeTraits.put("float64", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "String";
            typeTrait.objectType = "String";
            typeTrait.typeName = "String";
            typeTrait.defaultValue = "\"\"";
            typeTraits.put("string", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "Calendar";
            typeTrait.objectType = "Calendar";
            typeTrait.typeName = "Calendar";
            typeTrait.defaultValue = "null";
            typeTraits.put("datetime", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "byte[]";
            typeTrait.objectType = "Byte[]";
            typeTrait.typeName = "Bytes";
            typeTrait.defaultValue = "null";
            typeTraits.put("bytes", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "ArrayList";
            typeTrait.objectType = "ArrayList";
            typeTrait.typeName = "List";
            typeTrait.defaultValue = "null";
            typeTraits.put("list", typeTrait);
            
            typeTrait = new TypeTrait();
            typeTrait.nativeType = "HashMap";
            typeTrait.objectType = "HashMap";
            typeTrait.typeName = "Map";
            typeTrait.defaultValue = "null";
            typeTraits.put("map", typeTrait);
        }
    }
}
