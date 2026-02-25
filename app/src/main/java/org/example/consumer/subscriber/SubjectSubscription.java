package org.example.consumer.subscriber;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
class SubjectSubscription {
    private final Logger log = LoggerFactory.getLogger(SubjectSubscription.class);
    private final Connection natsConnectionSingleton;

    @Autowired
    SubjectSubscription(NatsConnectionSingleton natsConnectionSingleton) {
        this.natsConnectionSingleton = natsConnectionSingleton.getConnection();
    }

    public void newSubscription(String subject) {
        Dispatcher dispatcher = natsConnectionSingleton.createDispatcher(message -> {
            String payload = new String(message.getData(), StandardCharsets.UTF_8);
            log.info("Received message on [{}]: {}", subject, payload);
        });

        dispatcher.subscribe(subject);

        log.info("Subscribed to NATS subject [{}]", subject);
    }
}
