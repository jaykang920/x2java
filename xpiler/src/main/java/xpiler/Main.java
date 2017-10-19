// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

public class Main {
    public static void main(String[] args) {
        int index = Xpiler.getOptions().parse(args);
        if (index >= args.length) {
            System.out.println("xpiler: missing arguments");
            System.exit(2);
        }

        Xpiler xpiler = new Xpiler();
        while (index < args.length) {
            xpiler.process(args[index++]);
        }
        System.exit(xpiler.hasError() ? 1 : 0);
    }
}
