/*
 * Copyright (C) 2022 Tony Guo. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jxtras.android.build.tools.arpt;

import jxtras.android.build.tools.arpt.options.Options;
import jxtras.android.build.tools.util.Log;
import jxtras.android.build.tools.annotation.NonNull;

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
            final File ruleFile = options.getRuleFile().toFile();
            if (!ruleFile.exists() || !ruleFile.isFile()) {
                Log.error("arpt: ruleFile does not exist");
                return -1;
            }

            final String targetProduct = options.getTargetProduct();
            if (targetProduct == null || targetProduct.isEmpty()) {
                Log.error("arpt: target product not provided");
                return -1;
            }

            final List<Rule> rules = Rule.parseRules(ruleFile);
            if (rules.isEmpty()) {
                Log.info("arpt: no rules provided for target '" + targetProduct + "'");
                return 0;
            }

            final List<Path> resDirs = options.getResourceDirs();
            if (resDirs == null || resDirs.isEmpty()) {
                Log.info("arpt: resDirs is empty or not provided");
                return 0;
            }

            return prune(resDirs, targetProduct, rules);
        } catch (Exception e) {
            Log.error("arpt: exception occurred: " + e.getMessage());
            return -1;
        }
    }

    private static int prune(@NonNull List<Path> resDirs, @NonNull String targetProduct, @NonNull List<Rule> rules) {
        for (Path resDir : resDirs) {
            if (!Files.exists(resDir) || !Files.isDirectory(resDir)) {
                Log.warn("arpt: resDir does not exist: " + resDir);
                continue;
            }
            rules.forEach(rule -> {
                final String resourceType = rule.getResourceType();
                final String availability = rule.getAvailability();
                final Set<String> names = rule.getResourceNames();
                if (!availability.isEmpty() && !targetProduct.matches(availability)) {
                    Resolver resolver = Resolver.get(resourceType);
                    if (resolver != null) {
                        resolver.remove(resDir.toFile(), names);
                    }
                } else {
                    Log.info("arpt: target '" + targetProduct
                            + "' matches with regex '" + availability
                            + "', skip removing the following resources:");
                    for (String name : names) {
                        Log.info("    @" + resourceType + "/" + name);
                    }
                }
            });
        }
        return 0;
    }

    private static final String HELP = "Usage example: arpt -target name -rule rule dir\n"+
            "required options are:\n"+
            "dir             Prune all resources recursively below the specified directory\n"+
            "-target name    Specify target product name\n"+
            "-rule rule      Specify the rule for pruning\n";

    private static boolean validateOptions(Options options) {
        String err = null;

        if (options.getRuleFile() == null) {
            err = HELP;
        } else if (options.getTargetProduct() == null) {
            err = HELP;
        } else if (options.getResourceDirs().isEmpty()) {
            err = HELP;
        }

        if (err != null) {
            Log.error(err);
        }

        return err == null;
    }
}
