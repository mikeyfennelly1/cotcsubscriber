package org.example.consumer.stream.manager;

import io.nats.client.MessageHandler;
import org.example.consumer.stream.exception.StreamAlreadyExistsException;
import org.example.consumer.repository.StreamRepository;
import org.example.consumer.stream.exception.InvalidSubscriptionTreePathFormatException;
import org.example.consumer.stream.exception.TreePathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code SimpleStreamManager} manages time series data streams.
 * Specifically:
 * - NATS listeners and dispatchers.
 * - NATS durable consumers.
 * - Spring Webflux Streams (for SSE listening).
 * - Miscellaneous methods for querying the state of the stream management subsystem.
 */
@Component
class SimpleStreamManager implements StreamManager {
    private static final Logger logger = LoggerFactory.getLogger(SimpleStreamManager.class);
    private final StreamRepository streamRepository;
    private final DispatcherService dispatcherService;
    private final Map<String, Flux<ServerSentEvent<String>>> streamMap = new HashMap<>();

    @Autowired
    SimpleStreamManager(
            DispatcherService dispatcherService,
            StreamRepository streamRepository
    ) {
        this.dispatcherService = dispatcherService;
        this.streamRepository = streamRepository;
    }

    @Override
    public void createStream(String name, String parent) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException, StreamAlreadyExistsException {
        logger.debug("createStream - name='{}'", name);

        // validate that it is in the format '<stream_name>' or '<stream_name>.<child_stream_name>'
        InvalidSubscriptionTreePathFormatException.validate(name);
        if (streamAlreadyExists(name)) {
            throw new StreamAlreadyExistsException(name);
        }
        // any parent streams must exist
        if (!isRootStreamName(name)) {
            String parentStreamName = getParentStreamName(name);
            logger.debug("createStream - checking parent stream exists: '{}'", parentStreamName);
            if (!streamRepository.streamExists(parentStreamName)) {
                logger.debug("createStream - parent stream '{}' not found, throwing TreePathNotFoundException", parentStreamName);
                throw new TreePathNotFoundException(parentStreamName);
            }
        }

        logger.debug("recording new stream name in database: name={}", name);
        streamRepository.newStream(name);
        logger.debug("initializing dispatcher for stream: {}", name);
        initDispatcher(name);
        logger.info("createStream - stream '{}' created successfully", name);
    }

    @Override
    public void restoreStream(String name, String parent) {
        initDispatcher(name);
    }

    @Override
    public void deleteStream(String name) throws TreePathNotFoundException {
        logger.debug("deleteStream - name='{}'", name);
        if (!streamRepository.streamExists(name)) {
            logger.debug("deleteStream - stream '{}' not found, throwing TreePathNotFoundException", name);
            throw new TreePathNotFoundException(name);
        }
        streamRepository.deleteAllDescendants(name);
        logger.debug("deleteStream - deleted all descendants of '{}'", name);
        streamRepository.deleteByName(name);
        logger.debug("deleteStream - stream '{}' deleted successfully", name);
    }

    @Override
    public List<String> getAllStreamNames() {
        logger.debug("getAllStreamNames - fetching all stream names");
        List<String> names = streamRepository.findAll().stream()
                .map(stream -> stream.getName())
                .toList();
        logger.debug("getAllStreamNames - returning {} stream names", names.size());
        return names;
    }

    @Override
    public List<String> getChildStreams(String parentPath) throws TreePathNotFoundException, InvalidSubscriptionTreePathFormatException {
        logger.debug("getChildStreams - parentPath='{}'", parentPath);
        if (!streamRepository.streamExists(parentPath)) {
            logger.debug("getChildStreams - stream '{}' not found, throwing TreePathNotFoundException", parentPath);
            throw new TreePathNotFoundException(parentPath);
        }
        List<String> children = streamRepository.getChildren(parentPath).stream()
                .map(stream -> stream.getName())
                .toList();
        logger.debug("getChildStreams - found {} children for '{}'", children.size(), parentPath);
        return children;
    }

    void initDispatcher(String streamName) {
        logger.debug("initDispatcher - initializing NATS dispatcher for stream '{}'", streamName);
        MessageHandler handler = dispatcherService.newDispatcher("default");
        logger.debug("initDispatcher - NATS dispatcher initialized for stream '{}'", streamName);
    }

    String getParentStreamName(String name) {
        return name.substring(0, name.indexOf('.'));
    }

    boolean isRootStreamName(String name) {
        return !name.contains(".");
    }

    @Override
    public Flux<ServerSentEvent<String>> subscribeToStreamSSESink(String streamName) {
        logger.debug("subscribeToStream - creating live NATS subscription for stream '{}'", streamName);
        return flux;
    }

    @Override
    public boolean streamAlreadyExists(String streamName) {
        return this.streamRepository.streamExists(streamName);
    }
}
