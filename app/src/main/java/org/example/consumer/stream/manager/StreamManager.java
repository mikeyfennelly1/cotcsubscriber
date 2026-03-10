package org.example.consumer.stream.manager;

import org.example.consumer.stream.exception.InvalidSubscriptionTreePathFormatException;
import org.example.consumer.stream.exception.StreamAlreadyExistsException;
import org.example.consumer.stream.exception.TreePathNotFoundException;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface StreamManager {
    void createStream(String name, String parent) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, StreamAlreadyExistsException;
    void restoreStream(String name, String parent) throws TreePathNotFoundException;
    void deleteStream(String subscriptionName) throws TreePathNotFoundException;
    List<String> getAllStreamNames();
    List<String> getChildStreams(String parentPath) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException;
    Flux<ServerSentEvent<String>> subscribeToStreamSSESink(String streamName);
    boolean streamAlreadyExists(String streamName);
}
