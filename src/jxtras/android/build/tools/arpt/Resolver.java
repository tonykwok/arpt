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

import jxtras.android.build.tools.annotation.Nullable;
import jxtras.android.build.tools.util.Log;
import jxtras.android.build.tools.annotation.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public enum Resolver {
    STRING("string") {
        @Override
        public void remove(@NonNull File resDir, @NonNull Set<String> resNames) {
            final File[] valuesDirs = resDir.listFiles(VALUES_DIR_FILTER);
            if (valuesDirs != null) {
                Arrays.stream(valuesDirs).forEach(dir -> {
                    File[] xmlFiles = dir.listFiles(XML_FILE_FILTER);
                    if (xmlFiles != null) {
                        Arrays.stream(xmlFiles).forEach(file -> {
                            removeResourcesFromFile(file, getResourceType(), resNames);
                        });
                    }
                });
            }
        }
    },
    STRING_ARRAY("string-array") {
        @Override
        public void remove(@NonNull File resDir, @NonNull Set<String> resNames) {
            STRING.remove(resDir, resNames);
        }
    },
    PLURALS("plurals") {
        @Override
        public void remove(@NonNull File resDir, @NonNull Set<String> resNames) {
            STRING.remove(resDir, resNames);
        }
    },
    FILE("file") {
        @Override
        public void remove(@NonNull File resDir, @NonNull Set<String> resNames) {
            removeFilesInDirectory(resDir, resNames);
        }
    };

    private static final FileFilter DIR_FILTER = File::isDirectory;
    private static final FileFilter XML_FILE_FILTER = file -> file.getName().endsWith(".xml") && !DIR_FILTER.accept(file);
    private static final FileFilter VALUES_DIR_FILTER = file -> DIR_FILTER.accept(file) && file.getName().startsWith("values");

    private static final Map<String, Resolver> resolvers = new WeakHashMap<>(5);

    private final String resourceType;

    Resolver(@NonNull String resourceType) {
        this.resourceType = resourceType;
    }

    public @NonNull String getResourceType() {
        return resourceType;
    }

    public static @Nullable
    Resolver get(@NonNull String resourceType) {
        synchronized(resolvers) {
            return resolvers.computeIfAbsent(resourceType, s -> {
                for (Resolver resource : Resolver.values()) {
                    if (resource.resourceType.equals(s)) {
                        return resource;
                    }
                }
                return null;
            });
        }
    }

    protected abstract void remove(@NonNull File resDir, @NonNull Set<String> resNames);

    private static void removeFilesInDirectory(@NonNull File resDir, @NonNull Set<String> filePaths) {
        int count = 0;
        for (String path : filePaths) {
            File file = new File(resDir, path);
            Log.info("arpt: removing: " + file);
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    Log.info("arpt: failed");
                } else {
                    count++;
                    Log.info("arpt: file removed successfully");
                }
            } else {
                Log.info("arpt: file does not exist");
            }
        }
        Log.info("arpt: " + count + "/" + filePaths.size() + " file(s) removed");
    }

    private static void removeResourcesFromFile(@NonNull File xmlFile, @NonNull String resourceType, @NonNull Set<String> resNames) {
        Log.info("arpt: pruning: " + xmlFile);
        final List<Element> resNodes = new ArrayList<>();
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(xmlFile);

            final NodeList resources = document.getElementsByTagName(resourceType);
            if (resources == null || resources.getLength() == 0) {
                Log.info("arpt: resource(s) of type '" + resourceType + "' not found");
                return;
            }

            for (int i = 0, length = resources.getLength(); i < length; i++) {
                final Element resNode = (Element) resources.item(i);
                final String name = resNode.getAttribute("name");
                if (name != null && resNames.contains(name)) {
                    resNodes.add(resNode);
                }
            }

            if (!resNodes.isEmpty()) {
                for (Element resNode : resNodes) {
                    Log.info("arpt: '@" + resNode.getTagName() + "/" + resNode.getAttribute("name")
                            + "' removed successfully");
                    resNode.getParentNode().removeChild(resNode);
                }
                saveDocument(document, xmlFile);
            }
        } catch (Exception e) {
            Log.error("arpt: exception occurred: " + e.getMessage());
        }
        Log.info("arpt: " + resNodes.size() + " resource(s) removed");
    }

    private static void saveDocument(@NonNull Document document, @NonNull File xmlFile) {
        try (FileOutputStream fos = new FileOutputStream(xmlFile)) {
            document.setXmlStandalone(document.getXmlStandalone());
            document.setXmlVersion(document.getXmlVersion());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            //            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            //            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>".getBytes(StandardCharsets.UTF_8));
            //            out.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(fos);
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            Log.error("arpt: exception occurred: " + e.getMessage());
        }
    }
}
