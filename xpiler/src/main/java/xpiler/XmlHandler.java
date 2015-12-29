// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

class XmlHandler implements Handler {
    private static SAXParserFactory factory = SAXParserFactory.newInstance();

    public Result handle(String path) {
        Result result = new Result();
        try {
            SAXParser parser = factory.newSAXParser();
            Context context = new Context();

            File file = new File(path);
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");

            InputSource inputSource = new InputSource(reader);
            inputSource.setEncoding("UTF-8");

            parser.parse(inputSource, context);

            result.handled = true;
            result.doc = context.doc;
        } catch (UnknownDocumentException ude) {
            // Unknown format;
        } catch (Exception e) {
            result.handled = true;
            result.message = e.getMessage();
        }
        return result;
    }

    @SuppressWarnings("serial")
    private static class UnknownDocumentException extends SAXException {}

    private static class Context extends DefaultHandler {
        static interface StartHandler {
            void handle(Context context, Attributes attributes);
        }
        static interface EndHandler {
            void handle(Context context);
        }

        private static Map<String, StartHandler> startHandlers;
        private static Map<String, EndHandler> endHandlers;
        private static Map<String, String> defaultValues;

        static {
            startHandlers = new HashMap<String, StartHandler>();
            startHandlers.put("x2", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startRoot(attributes);
                }
            });
            startHandlers.put("consts", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startConsts(attributes);
                }
            });
            startHandlers.put("cell", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startCell(attributes);
                }
            });
            startHandlers.put("event", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startEvent(attributes);
                }
            });
            startHandlers.put("const", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startConstant(attributes);
                }
            });
            startHandlers.put("property", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startProperty(attributes);
                }
            });

            endHandlers = new HashMap<String, EndHandler>();
            endHandlers.put("x2", new EndHandler() {
                public void handle(Context context) { context.endRoot(); }
            });
            endHandlers.put("consts", new EndHandler() {
                public void handle(Context context) { context.endConsts(); }
            });
            endHandlers.put("cell", new EndHandler() {
                public void handle(Context context) { context.endCell(); }
            });
            endHandlers.put("event", new EndHandler() {
                public void handle(Context context) { context.endEvent(); }
            });
            endHandlers.put("constant", new EndHandler() {
                public void handle(Context context) { context.endConstant(); }
            });
            endHandlers.put("property", new EndHandler() {
                public void handle(Context context) { context.endProperty(); }
            });

            defaultValues = new HashMap<String, String>();
            defaultValues.put("int8", "0");
            defaultValues.put("int16", "0");
            defaultValues.put("int32", "0");
            defaultValues.put("int64", "0");
            defaultValues.put("string", "\"\"");
        }

        public Document doc;
        public Definition current;
        public StringBuilder text;

        public Context() {
            doc = new Document();
            text = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName,
            String qName, Attributes attributes) throws SAXException {
            if (doc.namespace == null && qName != "x2") {
                throw new UnknownDocumentException();
            }
            text.setLength(0);
            StartHandler handler = startHandlers.get(qName);
            if (handler != null) {
                handler.handle(this, attributes);
            } else {
                // unknown element name
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            EndHandler handler = endHandlers.get(qName);
            if (handler != null) {
                handler.handle(this);
            } else {
                // unknown element name
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
            throws SAXException {
            text.append(ch, start, length);
        }

        // startElement helpers

        private void startRoot(Attributes attributes) {
            String namespace = attributes.getValue("namespace");
            doc.namespace = (namespace != null ? namespace : "");
        }

        private void startConsts(Attributes attributes) {
            ConstsDef def = new ConstsDef();
            def.name = attributes.getValue("name");
            doc.getDefinitions().add(def);
            current = def;
        }

        private void startCell(Attributes attributes) {
            CellDef def = new CellDef();
            def.name = attributes.getValue("name");
            def.base = attributes.getValue("base");
            if (def.base == null || def.base.isEmpty()) {
                def.base = "x2.Cell";
            }
            doc.getDefinitions().add(def);
            current = def;
        }

        private void startEvent(Attributes attributes) {
            EventDef def = new EventDef();
            def.name = attributes.getValue("name");
            def.base = attributes.getValue("base");
            if (def.base == null || def.base.isEmpty()) {
                def.base = "x2.Event";
            }
            def.id = attributes.getValue("id");
            doc.getDefinitions().add(def);
            current = def;
        }

        private void startConstant(Attributes attributes) {
            ConstsDef def = (ConstsDef)current;
            ConstsDef.Constant constant = new ConstsDef.Constant();
            constant.name = attributes.getValue("name");
            def.getConstants().add(constant);
        }

        private void startProperty(Attributes attributes) {
            CellDef def = (CellDef)current;
            CellDef.Property prop = new CellDef.Property();
            prop.name = attributes.getValue("name");
            prop.type = attributes.getValue("type");
            def.getProperties().add(prop);
        }

        // endElement helpers

        private void endRoot() {
        }

        private void endConsts() {
            current = null;
        }

        private void endCell() {
            current = null;
        }

        private void endEvent() {
            current = null;
        }

        private void endConstant() {
            if (text.length() > 0) {
                ConstsDef def = (ConstsDef)current;
                List<ConstsDef.Constant> constants = def.getConstants();
                ConstsDef.Constant constant = constants.get(constants.size() - 1);
                constant.value = text.toString().trim();
            }
        }

        private void endProperty() {
            CellDef def = (CellDef)current;
            List<CellDef.Property> properties = def.getProperties();
            CellDef.Property prop = properties.get(properties.size() - 1);
            if (text.length() > 0) {
                prop.defaultValue = text.toString().trim();
            } else {
                prop.defaultValue = defaultValues.get(prop.type);
            }
        }
    }
}
