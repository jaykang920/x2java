// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.io.*;
import java.util.*;

class PathUtil {
    private PathUtil() {}

    public static String getCanonical(File file) {
        String result;
        try {
            result = file.getCanonicalPath();
        } catch (IOException e) {
            result = file.getPath();
        }
        return result;
    }

    public static String getBaseName(String filename) {
        int index = filename.lastIndexOf('.');
        return (index >= 0 ? filename.substring(0, index) : filename);
    }

    public static String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return (index >= 0 ? filename.substring(index) : "");
    }

    public static String getFilename(String path) {
        File file = new File(path);
        return file.getName();
    }

    public static String join(Collection<String> strings) {
        return StringUtil.join(File.separatorChar, strings);
    }

    public static String join(String former, String latter) {
        if (former == null || former.isEmpty()) {
            return latter;
        }
        if (latter == null || latter.isEmpty()) {
            return former;
        }
        return String.format("%s%c%s", former, File.separatorChar, latter);
    }
}
