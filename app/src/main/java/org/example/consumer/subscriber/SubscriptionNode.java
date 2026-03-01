package org.example.consumer.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import lombok.Getter;
import org.example.consumer.model.TimeSeriesRecord;
import org.example.consumer.model.dto.TimeSeriesMessageDTO;
import org.example.consumer.repository.TimeseriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubscriptionNode {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionNode.class);

    @Getter private final String name;
    @Getter final SubscriptionNode parent;
    final Map<String, SubscriptionNode> children = new LinkedHashMap<>();

    SubscriptionNode(String name, SubscriptionNode parent) {
        this.name = name;
        this.parent = parent;
    }

    boolean hasChild(String name) {
        return this.children.containsKey(name);
    }

    SubscriptionNode addChild(String name) {
        SubscriptionNode child = new SubscriptionNode(name, this);
        this.children.put(name, child);
        return child;
    }

    void init(String natsSubject, Connection natsConnection, ObjectMapper objectMapper, TimeseriesRepository repo) {
        logger.debug("subject {} initializing NATS subscription", natsSubject);
        Dispatcher dispatcher = natsConnection.createDispatcher(message -> {
            try {
                TimeSeriesMessageDTO dto = objectMapper.readValue(message.getData(), TimeSeriesMessageDTO.class);
                process(natsSubject, dto, repo);
            } catch (Exception e) {
                logger.debug("subject {} failed to parse message: {}", natsSubject, e.getMessage());
            }
        });
        dispatcher.subscribe(natsSubject);
        logger.debug("subject {} NATS subscription initialized", natsSubject);
    }

    private void process(String natsSubject, TimeSeriesMessageDTO dto, TimeseriesRepository repo) {
        logger.debug("subject {} received message {}", natsSubject, dto);
        Instant readTime = Instant.ofEpochSecond(dto.getReadTime());
        for (Map.Entry<String, Double> entry : dto.getValues().entrySet()) {
            TimeSeriesRecord record = new TimeSeriesRecord(null, entry.getKey(), entry.getValue().floatValue(), dto.getSource(), readTime);
            repo.save(record);
        }
    }

    void clearChildren() {
        this.children.clear();
    }

    String getFullyQualifiedName() {
        List<String> nameList = new LinkedList<>();

        SubscriptionNode curNode = this;
        while (curNode.hasParent()) {
            nameList.addFirst(curNode.name);
            curNode = curNode.parent;
        }

        return String.join(".", nameList);
    }

    SubscriptionNode getChild(String childNodeName) throws TreePathNotFoundException {
        if (this.hasChild(childNodeName)) {
            return this.children.get(childNodeName);
        } else {
            // TODO: make better exception for this, as this exception expects to receive a full path.
            //  ATP we only have the parent name and child name.
            throw new TreePathNotFoundException(childNodeName);
        }
    }

    List<SubscriptionNode> getChildren() {
        return this.children.values().stream().toList();
    }

    boolean hasParent() {
        return this.parent != null;
    }
}
