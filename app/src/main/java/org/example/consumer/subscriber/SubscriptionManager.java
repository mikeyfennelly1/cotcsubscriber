package org.example.consumer.subscriber;

import org.example.consumer.model.SourceCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SubscriptionManager {

    private static class SubscriptionNode {
        final String name;
        final Map<String, SubscriptionNode> children = new LinkedHashMap<>();

        SubscriptionNode(String name) {
            this.name = name;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    private final Map<String, SubscriptionNode> roots = new LinkedHashMap<>();

    public SubscriptionManager() {}

    /**
     * Adds a subscription in <parent>.<child> format.
     * If only a parent is given (no dot), it is added as a root node with no children.
     * Children are unique per parent — duplicate child names are silently ignored.
     */
    public void createNewCategorySubscription(String subject) {
        String[] parts = subject.split("\\.", 2);
        String parentName = parts[0];

        boolean newParent = !roots.containsKey(parentName);
        roots.putIfAbsent(parentName, new SubscriptionNode(parentName));
        if (newParent) {
            logger.debug("Added new root node: '{}'", parentName);
        } else {
            logger.debug("Root node '{}' already exists", parentName);
        }

        if (parts.length == 2) {
            String childName = parts[1];
            SubscriptionNode parent = roots.get(parentName);
            boolean newChild = !parent.children.containsKey(childName);
            parent.children.putIfAbsent(childName, new SubscriptionNode(childName));
            if (newChild) {
                logger.debug("Added child '{}' under parent '{}'", childName, parentName);
            } else {
                logger.debug("Child '{}' under parent '{}' already exists, skipping", childName, parentName);
            }
        }
    }

    /**
     * Returns all subjects in the tree.
     * For parents with children: returns "parent.child" for each child.
     * For parents with no children: returns "parent" on its own.
     */
    public List<String> getActiveSubscriptions() {
        List<String> subjects = new ArrayList<>();
        for (SubscriptionNode root : roots.values()) {
            if (root.children.isEmpty()) {
                subjects.add(root.name);
            } else {
                for (String child : root.children.keySet()) {
                    subjects.add(root.name + "." + child);
                }
            }
        }
        return List.copyOf(subjects);
    }

    /**
     * Returns the child names registered under the given parent, or an empty list
     * if the parent does not exist or has no children.
     */
    public List<String> getSubcategoriesByCategory(String parent) {
        SubscriptionNode node = roots.get(parent);
        if (node == null) {
            return List.of();
        }
        return List.copyOf(node.children.keySet());
    }

    public String getAvailableCategoriesAsString() {
        return Arrays.stream(SourceCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
