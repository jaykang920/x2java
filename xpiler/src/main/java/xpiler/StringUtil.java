// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

import java.util.*;

class StringUtil {
    private StringUtil() {}

    public static String firstToLower(String s) {
        if (isNullOrEmpty(s) || Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        char[] c = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    public static String firstToUpper(String s) {
        if (isNullOrEmpty(s) || Character.isUpperCase(s.charAt(0))) {
            return s;
        }
        char[] c = s.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.isEmpty());
    }

    public static String join(char separator, Collection<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(string);
        }
        return sb.toString();
    }
}
