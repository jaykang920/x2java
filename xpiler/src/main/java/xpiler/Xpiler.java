// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;

class Xpiler {
    private static Options options;
    private static Map<String, Handler> handlers;
    private static Map<String, Formatter> formatters;

    static {
        options = new Options();

        handlers = new HashMap<String, Handler>();
        handlers.put(".xml", new XmlHandler());

        formatters = new HashMap<String, Formatter>();
        formatters.put("java", new JavaFormatter());
    }

    public static Options getOptions() { return options; }
    public static Map<String, Formatter> getFormatters() { return formatters; }

    private Formatter formatter;
    private Stack<String> subDirs;
    private boolean error;

    public Xpiler() {
        formatter = formatters.get(options.getSpec());
        subDirs = new Stack<String>();
        error = false;
    }

    public boolean hasError() { return error; }

    public void process(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                processDir(file);
            } else {
                processFile(file);
            }
        } else {
            System.err.format("%s doesn't exist.\n", path);
            error = true;
        }
    }

    private void processDir(File dir) {
        System.out.format("Directory %s\n", PathUtil.getCanonical(dir));
        File[] entries = dir.listFiles();
        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (options.isRecursive()) {
                    subDirs.push(entry.getName());
                    processDir(entry);
                    subDirs.pop();
                }
            } else {
                processFile(entry);
            }
        }
    }

    private void processFile(File file) {
        String path = file.getPath();
        String filename = file.getName();
        String extension = PathUtil.getExtension(filename);
        String outDir;

        if (options.getOutDir() == null) {
            outDir = file.getParent();
        } else {
            outDir = PathUtil.join(options.getOutDir(), PathUtil.join(subDirs));
        }

        Handler handler = handlers.get(extension.toLowerCase());
        if (handler == null ||
            (!options.isForced() && formatter.isUpToDate(path, outDir))) {
            return;
        }
    
        Handler.Result result = handler.handle(path);
        if (result.handled == false) {
            return;
        }

        System.out.println(filename);
    
        if (result.handled == true && result.doc == null) {
            System.out.format("error: %s\n", result.message);
            error = true;
            return;
        }
    
        Document doc = result.doc;
        doc.baseName = PathUtil.getBaseName(filename);
    
        File dir = new File(outDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (formatter.format(doc, outDir) == false) {
            error = true;
        }
    }
}
