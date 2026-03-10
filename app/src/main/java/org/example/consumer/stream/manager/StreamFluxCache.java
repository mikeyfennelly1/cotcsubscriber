package org.example.consumer.stream.manager;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.Map;

public class StreamFluxCache {
    Map<String, Flux<ServerSentEvent<String>>> streamMap;


}
