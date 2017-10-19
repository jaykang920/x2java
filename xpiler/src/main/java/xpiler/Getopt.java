// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package xpiler;

/** Getopt is a clone of the GNU C getopt. */
public class Getopt {
    /** Long option item class. */
    public static class Option {
        private String name;
        private int hasArg;
        private int value;

        public Option(String name, int hasArg, int value) {
            this.name = name;
            this.hasArg = hasArg;
            this.value = value;
        }

        public String getName() { return name; }
        public int hasArg() { return hasArg; }
        public int getValue() { return value; }
    }

    enum Ordering {
        Permute, ReturnInOrder, RequireOrder
    };

    /** Indicates that the option does not take an argument. */
    public static final int NO_ARGUMENT = 0;
    /** Indicates that the option requires an argument. */
    public static final int REQUIRED_ARGUMENT = 1;
    /** Indicates that the option takes an optional argument. */
    public static final int OPTIONAL_ARGUMENT = 2;

    private String[] args;
    private String optstring;
    private Option[] longopts;

    private boolean done;
    private int opt;

    private String optarg;
    private int optind;
    private int optopt;
    private boolean opterr;

    private int longIndex;
    private boolean longOnly;

    private String nextchar;
    private int firstNonopt;
    private int lastNonopt;

    private Ordering ordering;

    private boolean posixlyCorrect;

    /** Returns the last option character.
     *  This enables the following usage:
     *  {@code
     *    while (getopt.Next() != -1) {
     *      int c = getopt.getOpt();
     *      ...
     *    }
     *  }
     */
    public int getOpt() { return opt; }

    /** Gets the value of the option argument, for those options that accept an
     *  argument.
     */
    public String getOptArg() { return optarg; }

    /** Gets the index of the next element to be processed in args.
     *  The initial value is 0. Unlike C getopt, optind is a read-only property
     *  of Getopt, and you should use reset() method in order to prepare Getopt
     *  for a new scanning, instead of resetting optind directly.
     *  <p>
     *  Once Getopt has processed all the options, you can use optind to determine
     *  where the remaining non-options begin in args.
     */
    public int getOptInd() { return optind; }

    /** Gets the option character, when Getopt encounters an unknown option
     *  character or an option without a required argument.
     */
    public int getOptOpt() { return optopt; }

    /** Gets or sets whether Getopt would print out error messages or not.
     *  If opterr is true (default), then Getopt prints an error message to the
     *  standard error stream if it encounters an unknown option or an option
     *  without a required argument.
     *  <p>
     *  If you set opterr to false, Getopt does not print any messages, but it
     *  still returns the character '?' to indicate an error.
     */
    public boolean getOptErr() { return opterr; }
    public void setOptErr(boolean value) { opterr = value; }

    /** Gets the index of the log option relative to longopts.
     *  A negative integer means that there is no long option matched.
     */
    public int getLongIndex() { return longIndex; }

    /** Gets or sets whether this Getopt would run in long_only mode.
     *  It's false by default.
     *  <p>
     *  If long_only is true, '-' as well as '--' can indicate a long option.
     *  If an option that starts with '-' (not '--') matches a short option, not
     *  a long option, then it is parsed as a short option.
     */
    public boolean getLongOnly() { return longOnly; }
    public void setLongOnly(boolean value) { longOnly = value; }

    public Getopt(String[] args, String optstring) {
        this(args, optstring, null);
    }

    public Getopt(String[] args, String optstring, Option[] longopts) {
        opterr = true;
        longOnly = false;

        posixlyCorrect = (System.getenv("POSIXLY_CORRECT") != null);

        reset(args, optstring, longopts);
    }

    /** Returns the next option character.
     *  When no more option is available, it returns -1. There may still be more
     *  non-options remaining.
     */
    public int next() {
        optarg = null;
        optopt = '?';
        longIndex = -1;

        if (done) {
            return (opt = -1);
        }

        if (nextchar == null || nextchar.isEmpty()) {
            if (locateNext()) {
                return opt;
            }
        }

        if (longopts != null &&
            (args[optind].startsWith("--") || (longOnly &&
            (args[optind].length() > 2 ||
            (optstring.indexOf(args[optind].charAt(1)) < 0))))) {
            if (checkLong()) {
                return opt;
            }
        }
        return (opt = checkShort());
    }

    public void reset(String[] args, String optstring) {
        reset(args, optstring, null);
    }

    public void reset(String[] args, String optstring, Option[] longopts) {
        if (args == null || optstring == null) {
            throw new IllegalArgumentException();
        }
        this.args = args;
        this.optstring = optstring;
        this.longopts = longopts;

        done = false;
        opt = -1;
        optarg = null;
        optind = 0;
        optopt = '?';
        longIndex = -1;

        nextchar = null;
        firstNonopt = lastNonopt = 0;

        if (optstring.startsWith("-")) {
            ordering = Ordering.ReturnInOrder;
            optstring = optstring.substring(1);
        } else if (optstring.startsWith("+")) {
            ordering = Ordering.RequireOrder;
            optstring = optstring.substring(1);
        } else if (posixlyCorrect) {
            ordering = Ordering.RequireOrder;
        } else {
            ordering = Ordering.Permute;
        }
    }

    private boolean locateNext() {
        if (ordering == Ordering.Permute) {
            if (lastNonopt != optind) {
                if (firstNonopt != lastNonopt) {
                    permute();
                } else {
                    firstNonopt = optind;
                }
            }
            // Skip any additional non-options.
            while (optind < args.length &&
                (!args[optind].startsWith("-") || args[optind].length() == 1)) {
                ++optind;
            }
            lastNonopt = optind;
        }

        // The special option '--' immediately stops the option scanning.
        if (optind < args.length && args[optind] == "--") {
            ++optind;
            if (firstNonopt == lastNonopt) {
                firstNonopt = optind;
            } else if (lastNonopt != optind) {
                permute();
            }
            optind = lastNonopt = args.length;
        }

        if (optind >= args.length) {
            if (firstNonopt != lastNonopt) {
                optind = firstNonopt;  // Let optind point at the first non-option.
            }
            opt = -1;
            return (done = true);
        }

        // Handle a non-option for non-permute ordering.
        if (!args[optind].startsWith("-") || args[optind].length() == 1) {
            if (ordering == Ordering.RequireOrder) {
                opt = -1;
                return true;
            }
            opt = 1;
            optarg = args[optind++];
            return true;
        }

        // Pick out the next option.
        nextchar = args[optind].substring(1);  // '-'
        if (longopts != null && nextchar.startsWith("-")) {
            nextchar = nextchar.substring(1);  // '--'
        }
        return false;
    }

    private boolean checkLong() {
        // Search for the end of option name.
        int index = nextchar.indexOf('=');
        String name = (index < 0 ? nextchar : nextchar.substring(0, index));

        // Scan the long option table.
        for (int i = 0; i < longopts.length; ++i) {
            Option option = longopts[i];
            if (option.getName().startsWith(name)) {
                if (option.getName() == name) {  // exact match
                    longIndex = i;
                    break;
                } else {  // non-exact match
                    if (longIndex < 0) {
                        longIndex = i;
                    } else {
                        longIndex = -2;
                        break;
                    }
                }
            }
        }

        if (longIndex > -1) {  // found
            Option option = longopts[longIndex];
            ++optind;
            if (index > 0) {
                if (option.hasArg() != 0) {
                    optarg = nextchar.substring(index + 1);
                } else {
                    if (opterr) {
                        System.err.println(String.format(
                            "Getopt: option '%s' doesn't allow an argument",
                            args[optind - 1]
                            ));
                    }
                    nextchar = null;
                    opt = '?';
                    return true;
                }
            } else if (option.hasArg() == REQUIRED_ARGUMENT) {
                if (optind < args.length) {
                    optarg = args[optind++];
                } else {
                    if (opterr) {
                        System.err.println(String.format(
                            "Getopt: option '%s' requires an argument",
                            args[optind - 1]
                            ));
                    }
                    nextchar = null;
                    opt = (optstring.startsWith(":") ? ':' : '?');
                    return true;
                }
            }
            nextchar = null;
            opt = option.getValue();
            return true;
        } else if (longIndex < -1) {  // ambiguous
            if (opterr) {
                System.err.println(String.format(
                    "Getopt: option '%s' is ambiguous", args[optind]
                    ));
            }
            nextchar = null;
            opt = '?';
            optopt = 0;
            ++optind;
            return true;
        }
        // No match found.
        if (longOnly == false || args[optind].startsWith("--") ||
            optstring.indexOf(nextchar.charAt(0)) < 0) {
            if (opterr) {
                System.err.println(String.format(
                    "Getopt: unrecognized option '%s'", args[optind]
                    ));
            }
            nextchar = null;
            opt = '?';
            optopt = 0;
            ++optind;
            return true;
        }
        return false;
    }

    private int checkShort() {
        char c = nextchar.charAt(0);
        nextchar = nextchar.substring(1);
        String optstr;
        int index = optstring.indexOf(c);
        if (index >= 0) {
            optstr = optstring.substring(index);
        } else {
            optstr = null;
        }

        // Increment optind, in advance, on the last character.
        if (nextchar.length() == 0) {
            ++optind;
        }

        // Sift out invalid options.
        if (optstr == null || c == ':') {
            if (opterr) {
                System.err.println(String.format(
                    "Getopt: %s option -- %c",
                    (posixlyCorrect ? "illegal" : "invalid"), c
                    ));
            }
            optopt = c;
            return '?';
        }

        // Check for an additional argument.
        if (optstr.length() > 1 && optstr.charAt(1) == ':') {
            if (optstr.length() > 2 && optstr.charAt(2) == ':') {  // optional
                if (nextchar.length() > 0) {
                    optarg = nextchar;  // take the rest
                    ++optind;
                }
            } else {  // required
                if (nextchar.length() > 0) {
                    optarg = nextchar;  // take the rest
                    ++optind;
                } else if (optind >= args.length) {
                    if (opterr) {
                        System.err.println(String.format(
                            "Getopt: option requires an argument -- %c", c
                            ));
                    }
                    optopt = c;
                    return (optstring.startsWith(":") ? ':' : '?');
                } else {
                    optarg = args[optind++];  // take the next
                }
            }
            nextchar = null;
        }
        return c;
    }

    private void permute() {
        int first = firstNonopt, middle = lastNonopt, last = optind;
        int next = middle;

        while (first != next) {
            String temp = args[first];
            args[first++] = args[next];
            args[next++] = temp;

            if (next == last) {
                next = middle;
            } else if (first == middle) {
                middle = next;
            }
        }

        firstNonopt += (optind - lastNonopt);
        lastNonopt = optind;
    }
}
