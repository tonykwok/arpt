package com.jxtras.android.build.tools.arpt;

import com.jxtras.android.build.tools.annotation.Nullable;
import com.jxtras.android.build.tools.util.Log;
import com.jxtras.android.build.tools.annotation.NonNull;
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
import java.io.OutputStream;
import java.util.*;

public enum Resource {
    STRING("string") {
        @Override
        public void removeValues(@NonNull File resDir, @NonNull Set<String> resourceValues) {
            File[] valuesDirs = resDir.listFiles(VALUES_DIR_FILTER);
            if (valuesDirs != null) {
                Arrays.stream(valuesDirs).forEach(dir -> {
                    File[] xmlFiles = dir.listFiles(XML_FILE_FILTER);
                    if (xmlFiles != null) {
                        Arrays.stream(xmlFiles).forEach(file -> {
                            removeTypedValuesFromXml(file, getResourceType(), resourceValues);
                        });
                    }
                });
            }
        }
    },
    STRING_ARRAY("string-array") {
        @Override
        public void removeValues(@NonNull File resDir, @NonNull Set<String> resourceValues) {
            STRING.removeValues(resDir, resourceValues);
        }
    },
    PLURALS("plurals") {
        @Override
        public void removeValues(@NonNull File resDir, @NonNull Set<String> resourceValues) {
            STRING.removeValues(resDir, resourceValues);
        }
    },
    FILE("file") {
        @Override
        public void removeValues(@NonNull File resDir, @NonNull Set<String> resourceValues) {
            removeFiles(resDir, resourceValues);
        }
    };

    private static final FileFilter DIR_FILTER = File::isDirectory;
    private static final FileFilter XML_FILE_FILTER = file -> file.getName().endsWith(".xml") && !DIR_FILTER.accept(file);
    private static final FileFilter VALUES_DIR_FILTER = file -> DIR_FILTER.accept(file) && file.getName().startsWith("values");

    private static final Map<String, Resource> types = new WeakHashMap<>(5);

    private final String resourceType;

    Resource(@NonNull String resourceType) {
        this.resourceType = resourceType;
    }

    public @NonNull String getResourceType() {
        return resourceType;
    }

    public static @Nullable Resource of(@NonNull String resourceType) {
        synchronized(types) {
            return types.computeIfAbsent(resourceType, s -> {
                for (Resource resource : Resource.values()) {
                    if (resource.resourceType.equals(s)) {
                        return resource;
                    }
                }
                return null;
            });
        }

    }

    protected abstract void removeValues(@NonNull File resDir, @NonNull Set<String> resourceValues);

    private static void removeFiles(@NonNull File resDir, @NonNull Set<String> filePathList) {
        int count = 0;
        for (String path : filePathList) {
            File file = new File(resDir, path);
            Log.info("arpt: removing: " + file);
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    Log.info("arpt: failed");
                } else {
                    count++;
                    Log.info("arpt: succeed");
                }
            } else {
                Log.info("arpt: file does not exist");
            }
        }
        Log.info("arpt: " + count + "/" + filePathList.size() + " file(s) removed");
    }

    private static void removeTypedValuesFromXml(@NonNull File xmlFile, @NonNull String resourceType, @NonNull Set<String> resourceValues) {
        Log.info("arpt: pruning: " + xmlFile);
        final List<Element> removableNodes = new ArrayList<>();
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(xmlFile);

            final NodeList resources = document.getElementsByTagName(resourceType);
            if (resources == null || resources.getLength() == 0) {
                Log.info("arpt: resource of type '" + resourceType + "' does not exist");
                return;
            }

            for (int i = 0, length = resources.getLength(); i < length; i++) {
                final Element resource = (Element) resources.item(i);
                final String name = resource.getAttribute("name");
                if (name != null && resourceValues.contains(name)) {
                    removableNodes.add(resource);
                }
            }

            if (!removableNodes.isEmpty()) {
                for (Element node : removableNodes) {
                    Log.info("arpt: " + node.getTagName()
                            + ": " + node.getAttribute("name") + " removed successfully");
                    node.getParentNode().removeChild(node);
                }
                saveDocument(document, xmlFile);
            }
        } catch (Exception e) {
            Log.error("arpt: exception occurred: " + e.getMessage());
        }
        Log.info("arpt: " + removableNodes.size() + " resource(s) removed");
    }

    private static void saveDocument(@NonNull Document document, @NonNull File xmlFile) {
        try (FileOutputStream fos = new FileOutputStream(xmlFile) {
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
