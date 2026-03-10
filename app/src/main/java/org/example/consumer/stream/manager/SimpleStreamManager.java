package org.example.consumer.stream.manager;

import io.nats.client.Connection;
import org.example.consumer.stream.exception.StreamAlreadyExistsException;
import org.example.consumer.repository.StreamRepository;
import org.example.consumer.stream.exception.InvalidStreamNameException;
import org.example.consumer.stream.exception.StreamNotFoundException;
import org.example.libb3project.dto.TimeSeriesRecordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.consumer.stream.utils.SubscriptionNameUtils.getParentStreamName;
import static org.example.consumer.stream.utils.SubscriptionNameUtils.isRootStreamName;

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
    private final Map<String, ManagedStream> managedStreams = new HashMap<>();
    private final Connection natsConnection;
    private final DispatcherFactory dispatcherFactory;

    @Autowired
    SimpleStreamManager(
            StreamRepository streamRepository,
            NatsConnectionSingleton natsConnSingleton,
            DispatcherFactory dispatcherFactory
    ) {
        this.streamRepository = streamRepository;
        this.natsConnection = natsConnSingleton.getConnection();
        this.dispatcherFactory = dispatcherFactory;
    }

    @Override
    public void createStream(String name, String parent) throws InvalidStreamNameException, StreamNotFoundException, StreamAlreadyExistsException {
        logger.debug("createStream - name='{}'", name);
        streamIsCreatable(name);
        initAndManageStream(name);
    }

    private void initAndManageStream(String name) {
        ManagedStream managedStream = ManagedStream.builder()
                        .natsStreamName(name)
                        .natsMessageHandler(
                                dispatcherFactory.getMessageHandler("default")
                        )
                        .build();
        managedStreams.put(name, managedStream);
        streamRepository.newStream(name);
    }

    private void streamIsCreatable(String name) throws InvalidStreamNameException, StreamAlreadyExistsException, StreamNotFoundException {
        // validate that it is in the format '<stream_name>' or '<stream_name>.<child_stream_name>'
        InvalidStreamNameException.validate(name);
        if (streamAlreadyExists(name)) {
            throw new StreamAlreadyExistsException(name);
        }
        // any parent streams must exist
        if (!isRootStreamName(name)) {
            String parentStreamName = getParentStreamName(name);
            logger.debug("createStream - checking parent stream exists: '{}'", parentStreamName);
            if (!streamRepository.streamExists(parentStreamName)) {
                logger.debug("createStream - parent stream '{}' not found, throwing TreePathNotFoundException", parentStreamName);
                throw new StreamNotFoundException(parentStreamName);
            }
        }
    }

    @Override
    public void restoreStream(String name, String parent) {
        // initialize flux stream
        // initialize dispatcher, pass flux stream to it so that it can write to that
    }

    @Override
    public void deleteStream(String name) throws StreamNotFoundException {
        logger.debug("deleteStream - name='{}'", name);
        if (!streamRepository.streamExists(name)) {
            logger.debug("deleteStream - stream '{}' not found, throwing TreePathNotFoundException", name);
            throw new StreamNotFoundException(name);
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
    public List<String> getChildStreams(String parentPath) throws InvalidStreamNameException, StreamNotFoundException {
        logger.debug("getChildStreams - parentPath='{}'", parentPath);
        if (!streamRepository.streamExists(parentPath)) {
            logger.debug("getChildStreams - stream '{}' not found, throwing TreePathNotFoundException", parentPath);
            throw new StreamNotFoundException(parentPath);
        }
        List<String> children = streamRepository.getChildren(parentPath).stream()
                .map(stream -> stream.getName())
                .toList();
        logger.debug("getChildStreams - found {} children for '{}'", children.size(), parentPath);
        return children;
    }

    @Override
    public Flux<TimeSeriesRecordDTO> getStreamSSESink(String streamName) {
        logger.debug("subscribeToStream - creating live NATS subscription for stream '{}'", streamName);
        return this.managedStreams.get(streamName).getFlux();
    }

    @Override
    public boolean streamAlreadyExists(String streamName) {
        return this.managedStreams.containsKey(streamName);
    }
}
