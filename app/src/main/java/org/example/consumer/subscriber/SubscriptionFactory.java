package org.example.consumer.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SubscriptionFactory implements SubscriptionManager {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionFactory.class);

    private final List<String> activeSubscriptionList = new ArrayList<String>();
    private final SubscriptionTree subscriptionTree;

    @Autowired
    public SubscriptionFactory(SubscriptionTree subscriptionTree) {
        this.subscriptionTree = subscriptionTree;
    }

    @Override public void newSubscription(String name, String parent) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, SubscriptionAlreadyExistsException {
        logger.debug("newSubscription - name='{}' parent='{}'", name, parent);
        subscriptionTree.newSubscription(name, parent);
        if (parent != null) {
            activeSubscriptionList.add(parent + "." + name);
        } else {
            activeSubscriptionList.add(name);
        }
        logger.debug("newSubscription - added to active list: {}", activeSubscriptionList);
    }

    @Override public void removeSubscription(String subscriptionName) {

    }

    @Override public List<String> getActiveSubscriptions() {
        logger.debug("getActiveSubscriptions - returning {} subscriptions", activeSubscriptionList.size());
        return this.activeSubscriptionList;
    }

    public List<SubscriptionNode> getChildSubscriptions(String parentPath) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException {
        logger.debug("getChildSubscriptions - parentPath='{}'", parentPath);
        SubscriptionNode parent = subscriptionTree.findNodeFromPath(parentPath);
        List<SubscriptionNode> children = parent.getChildren();
        logger.debug("getChildSubscriptions - found {} children", children.size());
        return children;
    }
}
