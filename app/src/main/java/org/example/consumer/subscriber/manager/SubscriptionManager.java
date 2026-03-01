package org.example.consumer.subscriber.manager;

import org.example.consumer.subscriber.exception.InvalidSubscriptionTreePathFormatException;
import org.example.consumer.subscriber.exception.SubscriptionAlreadyExistsException;
import org.example.consumer.subscriber.exception.TreePathNotFoundException;
import org.example.consumer.subscriber.manager.tree.SubscriptionNode;

import java.util.List;

public interface SubscriptionManager {
    void createSubscription(String name, String parent) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, SubscriptionAlreadyExistsException;
    void deleteSubscription(String subscriptionName);
    List<String> readAllSubscriptions();
    List<SubscriptionNode> getChildSubscriptions(String parentPath) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException ;
}
