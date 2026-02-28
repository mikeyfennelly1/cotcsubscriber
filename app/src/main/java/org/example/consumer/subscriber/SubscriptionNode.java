package org.example.consumer.subscriber;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SubscriptionNode {
    @Getter private final String name;
    @Getter final SubscriptionNode parent;
    final Map<String, SubscriptionNode> children = new LinkedHashMap<>();

    SubscriptionNode(String name, SubscriptionNode parent) {
        this.name = name;
        this.parent = parent;
    }

    boolean hasChild(String name) {
        return this.children.containsKey(name);
    }

    void addChild(String name) {
        this.children.put(name, new SubscriptionNode(name, this));
    }

    void clearChildren() {
        this.children.clear();
    }

    String getFullyQualifiedName() {
        List<String> nameList = new LinkedList<>();

        SubscriptionNode curNode = this;
        while (curNode.hasParent()) {
            nameList.addFirst(curNode.name);
            curNode = curNode.parent;
        }

        return String.join(".", nameList);
    }

    SubscriptionNode getChild(String childNodeName) throws TreePathNotFoundException {
        if (this.hasChild(childNodeName)) {
            return this.children.get(childNodeName);
        } else {
            // TODO: make better exception for this, as this exception expects to receive a full path.
            //  ATP we only have the parent name and child name.
            throw new TreePathNotFoundException(childNodeName);
        }
    }

    List<SubscriptionNode> getChildren() {
        return this.children.values().stream().toList();
    }

    boolean hasParent() {
        return this.parent != null;
    }
}
