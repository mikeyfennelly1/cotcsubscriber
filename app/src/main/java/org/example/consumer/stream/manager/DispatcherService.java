package org.example.consumer.stream.manager;

import io.nats.client.Connection;
import io.nats.client.MessageHandler;
import org.springframework.stereotype.Component;

@Component
class DispatcherService {
    DefaultDispatchMessageHandler defaultDispatchMessageHandler;
    DispatcherService() {

    }

    MessageHandler newDispatcher(String name, Connection natsConnection) {
        switch (name) {
            default:
                return defaultDispatchMessageHandler;
        }
    }
}
