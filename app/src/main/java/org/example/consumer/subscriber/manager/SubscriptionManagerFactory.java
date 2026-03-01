package org.example.consumer.subscriber.manager;

import org.example.consumer.subscriber.manager.tree.SubscriptionTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class SubscriptionManagerFactory {
    private final SubscriptionTree tree;

    @Autowired
    SubscriptionManagerFactory(SubscriptionTree tree) {
        this.tree = tree;
    }

    SubscriptionManager getManager(String managerName) {
        switch (managerName) {
            case "simple":
                return this.tree;
            case "tree":
                return this.tree;
            default:
                throw new IllegalArgumentException("manager of provided type does not exist: " + managerName);
        }
    }
}
