// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;

public class Main {
    private static Map<String, Handler> handlers;
    private static Map<String, Formatter> formatters;
    private static Options options;

    private Formatter formatter;
    private Stack<String> subDirs;
    private boolean error;

    static {
        handlers = new HashMap<String, Handler>();
        handlers.put(".xml", new XmlHandler());

        formatters = new HashMap<String, Formatter>();
        formatters.put("java", new JavaFormatter());

        options = new Options();
    }

    public Main() {
        formatter = formatters.get(options.getSpec());
        subDirs = new Stack<String>();
        error = false;
    }

    public static Map<String, Formatter> getFormatters() { return formatters; }
    public static Options getOptions() { return options; }
    
    public static void main(String[] args) {
        int index = Main.getOptions().parse(args);
        if (index >= args.length) {
            System.out.println("xpiler: missing arguments");
            System.exit(2);
        }

        Main main = new Main();
        while (index < args.length) {
            main.process(args[index++]);
        }
        System.exit(main.error() ? 1 : 0);
    }

    public boolean error() { return error; }

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
        if (handler == null) {
            return;
        }

        Handler.Result result = handler.handle(path);
        if (result.handled == false) {
            return;
        }

        if (result.handled == true && result.doc == null) {
            System.out.format("error: %s\n", result.message);
            error = true;
            return;
        }

        Document doc = result.doc;
        doc.inputPath = path;

        File dir = new File(outDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (formatter.format(doc, outDir) == false) {
            error = true;
        }
    }
}
