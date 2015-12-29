// Copyright (c) 2016 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

/** Document file handler interface. */
interface Handler {
    public static class Result {
        public boolean handled;
        public Document doc;
        public String message;
    }

    public Result handle(String path);
}
