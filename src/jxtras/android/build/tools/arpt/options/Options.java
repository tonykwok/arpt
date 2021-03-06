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
package jxtras.android.build.tools.arpt.options;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Options {

    private List<Path> resourceDirs = new ArrayList<>();

    private Path ruleFile;

    private String logLevel = "info";

    private String targetProduct;

    /**
     * Get the target product name
     */
    public String getTargetProduct() {
        return targetProduct;
    }

    /**
     * Get the path to the rule file
     */
    public Path getRuleFile() {
        return ruleFile;
    }

    /**
     * Get all resource locations for files to be pruned
     */
    public List<Path> getResourceDirs() {
        return resourceDirs;
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
        public void resourceDirs(List<Path> paths) {
            resourceDirs.addAll(paths);
        }

        @Override
        public void logLevel(String level) {
            logLevel = level;
        }

        @Override
        public void targetProduct(String name) {
            targetProduct = name;
        }

        @Override
        public void ruleFile(Path path) {
            ruleFile = path;
        }
    }
}