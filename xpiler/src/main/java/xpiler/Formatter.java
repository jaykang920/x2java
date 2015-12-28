// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;

/** Abstract base class for static output file formatters. */
abstract class Formatter {
    public abstract boolean format(Document doc, String outDir);

    public abstract String getDescription();

    public abstract boolean isUpToDate(String path, String outDir);
}

/** Abstract base class for concrete formatter contexts. */
abstract class FormatterContext {
    public Document doc;
    public PrintStream out;

    public abstract void formatReference(Reference reference);
    public abstract void formatConsts(ConstsDef def);
    public abstract void formatCell(CellDef def);
}
