package com.jxtras.android.build.tools.arpt.options;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public abstract class OptionHelper {

    /** Handle error */
    public abstract void reportError(String msg);

    /** Record a root of resources to be pruned */
    public abstract void resourcePaths(List<Path> path);

    /** Record desired log level */
    public abstract void logLevel(String level);

    /** Record target product name */
    public abstract void targetProduct(String name);

    /** Sets the path to rule file for pruning */
    public abstract void rulePath(Path path);

    /**
     * Traverses an array of arguments and performs the appropriate callbacks.
     *
     * @param args the arguments to traverse.
     */
    void traverse(String[] args) {
        ArgumentIterator iterator = new ArgumentIterator(Arrays.asList(args));

        nextArg:
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (arg.startsWith("-")) {
                for (Option opt : Option.values()) {
                    if (opt.processCurrent(iterator, this)) {
                        continue nextArg;
                    }
                }
            } else {
                resourcePaths(Arrays.asList(Paths.get(arg)));
            }
        }
    }
}