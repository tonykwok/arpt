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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public abstract class OptionHelper {

    /** Handle error */
    public abstract void reportError(String msg);

    /** Record locations of resources to be pruned */
    public abstract void resourceDirs(List<Path> path);

    /** Record desired log level */
    public abstract void logLevel(String level);

    /** Record target product name */
    public abstract void targetProduct(String name);

    /** Sets the path to rule file for pruning */
    public abstract void ruleFile(Path path);

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
                resourceDirs(Arrays.asList(Paths.get(arg)));
            }
        }
    }
}