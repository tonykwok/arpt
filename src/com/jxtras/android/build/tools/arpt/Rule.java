package com.jxtras.android.build.tools.arpt;

import com.jxtras.android.build.tools.annotation.Nullable;
import com.jxtras.android.build.tools.util.Log;
import com.jxtras.android.build.tools.annotation.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rule {

    private final Set<String> resourceValues;
    private final String resourceType;
    private final Set<String> targets;

    private Rule(@NonNull String resourceType) {
        this.resourceType = resourceType;
        this.resourceValues = new HashSet<>();
        this.targets = new HashSet<>();
    }

    public @NonNull Set<String> getResourceValues() {
        return resourceValues;
    }

    public @NonNull String getResourceType() {
        return resourceType;
    }

    public @NonNull Set<String> getTargets() {
        return targets;
    }

    public static List<Rule> parseRules(@NonNull File file) {
        final List<Rule> rules = new ArrayList<>();
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(file);

            final Element root = document.getDocumentElement();
            if (!"resources".equals(root.getTagName())) {
                Log.error("arpt: the name of root node must be 'resources'");
                return rules;
            }

            final NodeList resources = root.getChildNodes();
            if (resources == null) {
                Log.warn("arpt: the children of 'resources' node must not be null");
                return rules;
            }

            for (int i = 0, length = resources.getLength(); i < length; i++) {
                final Node node = resources.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final Element resource = (Element) node;

                    final String resourceType = resource.getTagName();
                    if (resourceType == null || resourceType.isEmpty()) {
                        Log.warn("arpt: resource type not defined");
                        continue;
                    }

                    final String availability = resource.getAttribute("availability");
                    if (availability == null || availability.isEmpty()) {
                        Log.warn("arpt: availability not defined, assume it's available to all");
                        continue;
                    }

                    final NodeList items = resource.getElementsByTagName("item");
                    if (items == null || items.getLength() == 0) {
                        Log.warn("arpt: resource items not defined, assume it's available to all");
                        continue;
                    }

                    final Rule rule = new Rule(resourceType);

                    final String[] targets = availability.split(",");
                    if (targets != null) {
                        for (String target : targets) {
                            rule.targets.add(target.trim());
                        }
                    }

                    for (int j = 0, count = items.getLength(); j < count; j++) {
                        final String value = items.item(j).getTextContent();
                        if (value != null && !value.isEmpty()) {
                            rule.resourceValues.add(value);
                        }
                    }

                    rules.add(rule);
                }
            }
        } catch (Exception e) {
            Log.error("arpt: failed to parse rules: " + e.getMessage());
        }
        return rules;
    }
}
