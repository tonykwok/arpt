package com.jxtras.android.build.tools.arpt.options;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Options {

    private List<Path> resourcePaths = new ArrayList<>();

    private Path rulePath;

    private String logLevel = "info";

    private String productName = "none";

    /**
     * Get the target product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Get the path to the configuration file
     */
    public Path getRulePath() {
        return rulePath;
    }

    /**
     * Get all resource locations for files to be pruned
     */
    public List<Path> getResourcePaths() {
        return resourcePaths;
    }

    /**
     * Get the log level.
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Parses the given argument array and returns a corresponding Options instance.
     */
    public static Options parseArgs(String... args) {
        Options options = new Options();
        options.new ArgDecoderOptionHelper().traverse(args);
        return options;
    }

    // OptionHelper that records the traversed options in this Options instance.
    private class ArgDecoderOptionHelper extends OptionHelper {

        @Override
        public void reportError(String msg) {
            throw new IllegalArgumentException(msg);
        }

        @Override
        public void resourcePaths(List<Path> paths) {
            resourcePaths.addAll(paths);
        }

        @Override
        public void logLevel(String level) {
            logLevel = level;
        }

        @Override
        public void productName(String name) {
            productName = name;
        }

        @Override
        public void rulePath(Path path) {
            rulePath = path;
        }
    }
}