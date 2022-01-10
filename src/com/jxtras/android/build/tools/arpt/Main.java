package com.jxtras.android.build.tools.arpt;

import com.jxtras.android.build.tools.arpt.options.Options;
import com.jxtras.android.build.tools.util.Log;
import com.jxtras.android.build.tools.annotation.NonNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        int rc = main.go(args, System.out, System.err);
        System.exit(rc);
    }

    private int go(final String[] args, final PrintStream out, final PrintStream err) {
        Log.initializeLog(out, err);

        Options options;
        try {
            options = Options.parseArgs(args);
        } catch (IllegalArgumentException e) {
            Log.error(e.getMessage());
            return -1;
        }

        Log.setLogLevel(options.getLogLevel());

        if (!validateOptions(options)) {
            return -1;
        }

        try {
            final File ruleFile = options.getRulePath().toFile();
            if (!ruleFile.exists() || !ruleFile.isFile()) {
                Log.error("arpt: ruleFile does not exist");
                return -1;
            }

            final String targetProduct = options.getTargetProduct();
            if (targetProduct == null || targetProduct.isEmpty()) {
                Log.error("arpt: target product not provided");
                return -1;
            }

            final List<Rule> rules = Rule.parseRules(ruleFile, targetProduct);
            if (rules.isEmpty()) {
                Log.info("arpt: no rules provided for target '" + targetProduct + "'");
                return 0;
            }

            final List<Path> resDirs = options.getResourcePaths();
            if (resDirs == null || resDirs.isEmpty()) {
                Log.info("arpt: resDirs is empty or not provided");
                return 0;
            }

            return prune(resDirs, rules);
        } catch (Exception e) {
            Log.error("arpt: exception occurred: " + e.getMessage());
            return -1;
        }
    }

    private static int prune(@NonNull List<Path> resDirs, @NonNull List<Rule> rules) {
        for (Path resDir : resDirs) {
            if (!Files.exists(resDir) || !Files.isDirectory(resDir)) {
                Log.warn("arpt: resDir does not exist: " + resDir);
                continue;
            }
            rules.forEach(rule -> {
                final String resourceType = rule.getResourceType();
                final Set<String> resourceValues = rule.getResourceValues();
                Resource resource = Resource.of(resourceType);
                if (resource != null) {
                    resource.removeValues(resDir.toFile(), resourceValues);
                }
            });
        }
        return 0;
    }

    private static final String HELP = "Usage example: arpt dir -target name -rule rule\n"+
            "required options are:\n"+
            "dir             Prune all resources recursively below the specified directory\n"+
            "-target name    Specify target product name\n"+
            "-rule rule      Specify the rule for pruning\n";

    private static boolean validateOptions(Options options) {
        String err = null;

        if (options.getRulePath() == null) {
            err = HELP;
        } else if (options.getTargetProduct() == null) {
            err = HELP;
        } else if (options.getResourcePaths().isEmpty()) {
            err = HELP;
        }

        if (err != null) {
            Log.error(err);
        }

        return err == null;
    }
}
