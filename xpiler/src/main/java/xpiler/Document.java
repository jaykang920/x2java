// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.util.*;

/** Represents a single definition document. */
class Document {
    public String namespace;
    public String inputPath;

    private List<Reference> references = new Vector<Reference>();
    private List<Definition> definitions = new Vector<Definition>();

    public List<Reference> getReferences() { return references; }
    public List<Definition> getDefinitions() { return definitions; }
}
