package jxtras.android.build.tools.arpt.options;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public enum Option {
    VERSION("-version", "Print arpt version") {
        @Override
        protected void processMatching(ArgumentIterator iterator, OptionHelper helper) {
            System.out.println("arpt version 0.1");
        }
    },
    DIRS("-dirs", "Location of resource dirs to be pruned") {
        @Override
        protected void processMatching(ArgumentIterator iterator, OptionHelper helper) {
            List<Path> paths = getFileListArg(iterator, helper);
            if (paths != null) {
                helper.resourceDirs(paths);
            }
        }
    },
    TARGET("-target", "Specify target product name") {
        @Override
        protected void processMatching(ArgumentIterator iterator, OptionHelper helper) {
            String name = getNameArg(iterator, helper);
            if (name != null) {
                helper.targetProduct(name);
            }
        }
    },
    RULE("-rule", "Specify path to the rule file used for resource pruning") {
        @Override
        protected void processMatching(ArgumentIterator iterator, OptionHelper helper) {
            Path path = getFileArg(iterator, helper, true, false);
            if (path != null) {
                helper.ruleFile(path);
            }
        }
    },
    LOG("-log:", "Specify logging level") {
        @Override
        protected void processMatching(ArgumentIterator iterator, OptionHelper helper) {
            helper.logLevel(iterator.current().substring(arg.length()));
        }
    },
    VERBOSE("-verbose", "Set verbosity level to \"info\"") {
        @Override
        protected void processMatching(ArgumentIterator iterator, OptionHelper helper) {
            helper.logLevel("info");
        }
    };

    public final String arg;
    public final String description;

    Option(String arg, String description) {
        this.arg = arg;
        this.description = description;
    }

    /**
     * Retrieve and verify syntax of file list argument.
     */
    List<Path> getFileListArg(ArgumentIterator iterator, OptionHelper helper) {
        if (!iterator.hasNext()) {
            helper.reportError(arg + " must be followed by a list of files " +
                              "separated by " + File.pathSeparator);
            return null;
        }
        List<Path> result = new ArrayList<>();
        for (String pathStr : iterator.next().split(File.pathSeparator)) {
            result.add(Paths.get(pathStr));
        }
        return result;
    }

    /**
     * Retrieve and verify syntax of file argument.
     */
    Path getFileArg(ArgumentIterator iterator, OptionHelper helper, boolean fileAcceptable, boolean dirAcceptable) {
        if (!iterator.hasNext()) {
            String errmsg = arg + " must be followed by ";
            if (fileAcceptable && dirAcceptable) errmsg += "a file or directory.";
            else if (fileAcceptable) errmsg += "a file.";
            else if (dirAcceptable)  errmsg += "a directory.";
            else throw new IllegalArgumentException("File or directory must be acceptable.");
            helper.reportError(errmsg);
            return null;
        }

        return Paths.get(iterator.next());
    }

    /**
     * Retrieve the next word-like argument.
     */
    String getNameArg(ArgumentIterator iterator, OptionHelper helper) {
        if (!iterator.hasNext() || !iterator.peek().matches("\\w+")) {
            helper.reportError(arg + " must be followed by a word");
            return null;
        }

        return iterator.next();
    }

    public boolean hasOption() {
        return arg.endsWith(":");
    }

    /**
     * Process current argument of argIterator.
     *
     * It's final, since the option customization is typically done in
     * processMatching.
     *
     * @param iterator Iterator to read current and succeeding arguments from.
     * @param helper The helper to report back to.
     * @return true iff the argument was processed by this option.
     */
    public final boolean processCurrent(ArgumentIterator iterator, OptionHelper helper) {
        String fullArg = iterator.current(); // "-dirs" or "-log:level"
        if (hasOption() ? fullArg.startsWith(arg) : fullArg.equals(arg)) {
            processMatching(iterator, helper);
            return true;
        }
        // Did not match
        return false;
    }

    /**
     * Called by process if the current argument matches this option.
     */
    protected abstract void processMatching(ArgumentIterator iterator, OptionHelper helper);
}