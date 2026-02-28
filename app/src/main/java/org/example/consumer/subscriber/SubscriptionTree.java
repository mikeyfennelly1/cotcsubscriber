package org.example.consumer.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SubscriptionTree implements SubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionTree.class);
    private final Map<String, SubscriptionNode> treeRootChildren = new LinkedHashMap<>();
    private final SubscriptionNameUtils subscriptionNameUtils;

    SubscriptionTree(SubscriptionNameUtils subscriptionNameUtils) {
        this.subscriptionNameUtils = subscriptionNameUtils;
    }

    @Override public void removeSubscription(String subscriptionName) {
    }

    @Override public void newSubscription(String name, String parentFullName) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, SubscriptionAlreadyExistsException {
        logger.debug("newSubscription - name='{}' parentFullName='{}'", name, parentFullName);
        if (parentFullName == null && !this.treeRootChildren.containsKey(name)) {
            newRoot(name);
        } else if (parentFullName == null && this.treeRootChildren.containsKey(name)) {
            logger.debug("newSubscription - root '{}' already exists, throwing SubscriptionAlreadyExistsException", name);
            throw new SubscriptionAlreadyExistsException(name);
        } else {
            SubscriptionNode parentNode = findNodeFromPath(parentFullName);

            if (!parentNode.hasChild(name)) {
                parentNode.addChild(name);
                logger.debug("newSubscription - added child '{}' under parent '{}'", name, parentFullName);
            } else {
                logger.debug("newSubscription - child '{}' under parent '{}' already exists, skipping", name, parentFullName);
            }
        }
        return;
    }

    @Override public List<String> getActiveSubscriptions() {
        logger.debug("getActiveSubscriptions - building subject list from {} root(s)", treeRootChildren.size());
        List<String> subjects = new ArrayList<>();
        for (SubscriptionNode root : treeRootChildren.values()) {
            if (root.children.isEmpty()) {
                subjects.add(root.getName());
            } else {
                for (String child : root.children.keySet()) {
                    subjects.add(root.getName() + "." + child);
                }
            }
        }
        logger.debug("getActiveSubscriptions - returning {} subjects: {}", subjects.size(), subjects);
        return List.copyOf(subjects);
    }

    SubscriptionNode findNodeFromPath(String nodeFullPath) throws InvalidSubscriptionTreePathFormatException, TreePathNotFoundException {
        logger.debug("findNodeFromPath - nodeFullPath='{}'", nodeFullPath);
        List<String> subjectNames = SubscriptionNameUtils.listOfSubjectsFromTreePath(nodeFullPath);

        SubscriptionNameUtils.NameBuilder currentAbsolutePath = subscriptionNameUtils.builder(subjectNames.getFirst());
        String curNodeName = subjectNames.getFirst();
        SubscriptionNode currentNode = this.treeRootChildren.get(curNodeName);
        if (currentNode == null) {
            throw new TreePathNotFoundException(subjectNames.getFirst());
        }
        for (int i = 1; i < subjectNames.size(); i++) {
            // item at index i is guaranteed to exist from loop condition
            String nextNodeName = subjectNames.get(i);
            // add the current node name to the list
            currentAbsolutePath.addName(curNodeName);
            // increment the currentNode to be the next in the list
            //  throws TreePathNotFoundException if the child doesn't exist
            currentNode = currentNode.getChild(nextNodeName);
            logger.trace("findNodeFromPath - [i={}] traversed to node '{}'", i, curNodeName);
        }

        logger.debug("findNodeFromPath - resolved node: '{}'", currentNode.getName());
        return currentNode;
    }

    private void newRoot(String name) {
        if (rootNodeExists(name)) {
            logger.debug("newRoot - root node '{}' already exists", name);
        } else {
            treeRootChildren.put(name, new SubscriptionNode(name, null));
            logger.debug("newRoot - added root node: '{}'", name);
        }
        return;
    }

    boolean rootNodeExists(String name) {
       return treeRootChildren.containsKey(name);
    }
}
