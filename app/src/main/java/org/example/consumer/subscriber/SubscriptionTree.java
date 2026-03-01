package org.example.consumer.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import org.example.consumer.repository.TimeseriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SubscriptionTree implements SubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionTree.class);
    private final Map<String, SubscriptionNode> treeRootChildren = new LinkedHashMap<>();
    private final SubscriptionNameUtils subscriptionNameUtils;
    private final List<String> activeSubscriptionList = new ArrayList<String>();
    private final Connection natsConnection;
    private final ObjectMapper objectMapper;
    private final TimeseriesRepository timeseriesRepository;

    @Autowired
    SubscriptionTree(SubscriptionNameUtils subscriptionNameUtils, NatsConnectionSingleton natsConnectionSingleton, ObjectMapper objectMapper, TimeseriesRepository timeseriesRepository) {
        this.subscriptionNameUtils = subscriptionNameUtils;
        this.natsConnection = natsConnectionSingleton.getConnection();
        this.objectMapper = objectMapper;
        this.timeseriesRepository = timeseriesRepository;
    }

    @Override public void deleteSubscription(String subscriptionName) {
    }

    @Override public void createSubscription(String name, String parentFullName) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, SubscriptionAlreadyExistsException {
        logger.debug("newSubscription - name='{}' parentFullName='{}'", name, parentFullName);
        if (parentFullName == null && !this.treeRootChildren.containsKey(name)) {
            newRoot(name);
        } else if (parentFullName == null && this.treeRootChildren.containsKey(name)) {
            logger.debug("newSubscription - root '{}' already exists, throwing SubscriptionAlreadyExistsException", name);
            throw new SubscriptionAlreadyExistsException(name);
        } else {
            SubscriptionNode parentNode = findNodeFromPath(parentFullName);

            if (!parentNode.hasChild(name)) {
                String newNodeFullName = parentFullName + "." + name;
                SubscriptionNode child = parentNode.addChild(name);
                child.init(newNodeFullName, natsConnection, objectMapper, timeseriesRepository);
                if (parentFullName != null) {
                    activeSubscriptionList.add(newNodeFullName);
                } else {
                    activeSubscriptionList.add(name);
                }
                logger.debug("newSubscription - added to active list: {}", activeSubscriptionList);
                logger.debug("newSubscription - added child '{}' under parent '{}'", name, parentFullName);
            } else {
                logger.debug("newSubscription - child '{}' under parent '{}' already exists, skipping", name, parentFullName);
            }
        }
        return;
    }

    @Override public List<String> readAllSubscriptions() {
        logger.debug("getActiveSubscriptions - returning {} subscriptions", activeSubscriptionList.size());
        return this.activeSubscriptionList;
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
            SubscriptionNode root = new SubscriptionNode(name, null);
            treeRootChildren.put(name, root);
            root.init(name, natsConnection, objectMapper, timeseriesRepository);
            logger.debug("newRoot - added root node: '{}'", name);
        }
        return;
    }

    boolean rootNodeExists(String name) {
       return treeRootChildren.containsKey(name);
    }
}
