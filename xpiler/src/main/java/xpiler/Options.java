// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;

class Options {
    private static final String DEFAULT_SPEC = "java";

    private String spec = DEFAULT_SPEC;
    private String outDir;
    private boolean forced;
    private boolean recursive;

    public String getSpec() { return spec; }
    public String getOutDir() { return outDir; }
    public boolean isForced() { return forced; }
    public boolean isRecursive() { return recursive; }

    public int parse(String[] args) {
        Getopt.Option[] longopts = new Getopt.Option[] {
            new Getopt.Option("spec", Getopt.REQUIRED_ARGUMENT, 's'),
            new Getopt.Option("out-dir", Getopt.REQUIRED_ARGUMENT, 'o'),
            new Getopt.Option("recursive", Getopt.NO_ARGUMENT, 'r'),
            new Getopt.Option("force", Getopt.NO_ARGUMENT, 'f'),
            new Getopt.Option("help", Getopt.NO_ARGUMENT, 'h')
        };

        Getopt getopt = new Getopt(args, "s:o:rfh", longopts);
        while (getopt.next() != -1) {
            switch (getopt.getOpt()) {
            case 's':
                spec = getopt.getOptArg().toLowerCase();
                if (!Xpiler.getFormatters().containsKey(spec)) {
                    System.err.format("Unknown target formatter specified: %s\n", spec);
                    System.exit(1);
                }
                break;
            case 'o':
                outDir = getopt.getOptArg();
                break;
            case 'r':
                recursive = true;
                break;
            case 'f':
                forced = true;
                break;
            case 'h':
                printUsage();
                System.exit(2);
                break;
            default:
                break;
            }
        }
        return getopt.getOptInd();
    }
  
    private static void printUsage() {
        PrintStream out = System.out;
        out.println("usage: xpiler (options) [path...]");
        out.println(" options:");
        out.println("  -o (--out-dir) dir : specify the output root directory");
        out.println("  -r (--recursive)   : process subdirectories recursively");
        out.println("  -f (--force)       : force all to be recompiled");
        out.println("  -h (--help)        : print this message and quit");
        out.println("  -s (--spec) spec   : specify the target formatter");

        for (Map.Entry<String, Formatter> entry : Xpiler.getFormatters().entrySet()) {
            out.format("%20s : %s", entry.getKey(), entry.getValue().description());
            if (entry.getKey() == DEFAULT_SPEC) {
                out.print(" (default)");
            }
            out.println();
        }
    }
}
