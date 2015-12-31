// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/** XML document file handler. */
class XmlHandler implements Handler {
    private static SAXParserFactory factory = SAXParserFactory.newInstance();

    public Result handle(String path) {
        Result result = new Result();
        try {
            SAXParser saxParser = factory.newSAXParser();
            Context context = new Context();

            File file = new File(path);
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");

            InputSource inputSource = new InputSource(reader);
            inputSource.setEncoding("UTF-8");

            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setProperty(
                "http://xml.org/sax/properties/lexical-handler", context);

            saxParser.parse(inputSource, context);

            result.handled = true;
            result.doc = context.doc;
        } catch (UnknownDocumentException ude) {
            // unknown format
        } catch (Exception e) {
            result.handled = true;
            result.message = e.getMessage();
        }
        return result;
    }

    @SuppressWarnings("serial")
    private static class UnknownDocumentException extends SAXException {}

    private static class Context
            extends DefaultHandler implements LexicalHandler {
        static interface StartHandler {
            void handle(Context context, Attributes attributes);
        }
        static interface EndHandler {
            void handle(Context context);
        }

        private static Map<String, StartHandler> startHandlers;
        private static Map<String, EndHandler> endHandlers;

        static {
            startHandlers = new HashMap<String, StartHandler>();
            startHandlers.put("x2", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startRoot(attributes);
                }
            });
            startHandlers.put("ref", new StartHandler() {
                public void handle(Context context, Attributes attributes) {
                    context.startReference(attributes);
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
            endHandlers.put("ref", new EndHandler() {
                public void handle(Context context) { context.endReference(); }
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
            endHandlers.put("const", new EndHandler() {
                public void handle(Context context) { context.endConstant(); }
            });
            endHandlers.put("property", new EndHandler() {
                public void handle(Context context) { context.endProperty(); }
            });
        }

        public Document doc;
        private Definition current;
        private StringBuilder text;
        private String comment;

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
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if (length > 0) {
                text.append(ch, start, length);
            }
        }

        // startElement helpers

        private void startRoot(Attributes attributes) {
            String namespace = attributes.getValue("namespace");
            doc.namespace = (namespace != null ? namespace : "");
        }

        private void startReference(Attributes attributes) {
            Reference reference = new Reference();
            reference.target = attributes.getValue("target");
            doc.getReferences().add(reference);
        }

        private void startConsts(Attributes attributes) {
            String name = attributes.getValue("name");
            String type = attributes.getValue("type");

            if (name == null || name.isEmpty()) {
                return;
            }
            if (type == null || type.isEmpty()) {
                type = "int32";  // default type
            }

            ConstsDef def = new ConstsDef();
            def.name = name;
            def.type = type;
            doc.getDefinitions().add(def);
            current = def;
            if (comment != null) {
                def.comment = comment;
            }
        }

        private void startCell(Attributes attributes) {
            CellDef def = new CellDef();
            def.name = attributes.getValue("name");
            def.base = attributes.getValue("base");
            doc.getDefinitions().add(def);
            current = def;
            if (comment != null) {
                def.comment = comment;
            }
        }

        private void startEvent(Attributes attributes) {
            EventDef def = new EventDef();
            def.name = attributes.getValue("name");
            def.base = attributes.getValue("base");
            def.id = attributes.getValue("id");
            doc.getDefinitions().add(def);
            current = def;
            if (comment != null) {
                def.comment = comment;
            }
        }

        private void startConstant(Attributes attributes) {
            String name = attributes.getValue("name");

            if (name == null || name.isEmpty()) {
                return;
            }

            ConstsDef def = (ConstsDef)current;
            ConstsDef.Constant constant = new ConstsDef.Constant();
            constant.name = name;
            def.getConstants().add(constant);
            if (comment != null) {
                constant.comment = comment;
            }
        }

        private void startProperty(Attributes attributes) {
            String name = attributes.getValue("name");
            String type = attributes.getValue("type");

            if (name == null || name.isEmpty()) {
                return;
            }
            if (type == null || type.isEmpty()) {
                return;
            }

            TypeSpec typeSpec = Types.parse(type);
            if (typeSpec == null) {
                return;
            }

            CellDef def = (CellDef)current;
            CellDef.Property prop = new CellDef.Property();
            prop.name = name;
            prop.typeSpec = typeSpec;
            def.getProperties().add(prop);
            if (comment != null) {
                prop.comment = comment;
            }
        }

        // endElement helpers

        private void endRoot() {
        }

        private void endReference() {
        }

        private void endConsts() {
            current = null;
            comment = null;
        }

        private void endCell() {
            current = null;
            comment = null;
        }

        private void endEvent() {
            current = null;
            comment = null;
        }

        private void endConstant() {
            if (text.length() > 0) {
                ConstsDef def = (ConstsDef)current;
                List<ConstsDef.Constant> constants = def.getConstants();
                ConstsDef.Constant constant = constants.get(constants.size() - 1);
                constant.value = text.toString().trim();
            }
            comment = null;
        }

        private void endProperty() {
            CellDef def = (CellDef)current;
            List<CellDef.Property> properties = def.getProperties();
            CellDef.Property prop = properties.get(properties.size() - 1);
            if (text.length() > 0) {
                prop.defaultValue = text.toString().trim();
            } else {
                prop.defaultValue = "";
            }
            comment = null;
        }

        // LexicalHandler implementation

        public void comment(char[] ch, int start, int length) throws SAXException {
            comment = new String(ch, start, length);
        }

        public void startCDATA() throws SAXException {
        }

        public void endCDATA() throws SAXException {
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
        }

        public void endDTD() throws SAXException {
        }

        public void startEntity(String name) throws SAXException {
        }

        public void endEntity(String name) throws SAXException {
        }
    }
}
