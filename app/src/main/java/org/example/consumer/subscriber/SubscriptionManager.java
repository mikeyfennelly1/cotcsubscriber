package org.example.consumer.subscriber;

import java.util.List;

public interface SubscriptionManager {
    void createSubscription(String name, String parent) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, SubscriptionAlreadyExistsException;
    void deleteSubscription(String subscriptionName);
    List<String> readAllSubscriptions();
}
