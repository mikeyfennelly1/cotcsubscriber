package org.cotc.controller;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.AccountStatistics;
import io.nats.client.api.ApiStats;
import io.nats.client.api.ConsumerInfo;
import io.swagger.v3.oas.annotations.Operation;
import org.cotc.config.NatsConfiguration;
import org.cotc.model.NatsJetstreamHealthResponse;
import org.cotc.model.StreamStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/nats")
public class NatsHealthController {

    private static final Logger logger = LoggerFactory.getLogger(NatsHealthController.class);

    private final NatsConfiguration natsConfiguration;

    @Autowired
    public NatsHealthController(NatsConfiguration natsConfiguration) {
        this.natsConfiguration = natsConfiguration;
    }

    @Operation(summary = "NATS JetStream health", description = "Returns JetStream account statistics from the connected NATS server.")
    @GetMapping("/health")
    public ResponseEntity<NatsJetstreamHealthResponse> health() {
        logger.debug("GET /api/nats/health - fetching JetStream account statistics");
        try {
            JetStreamManagement jsm = natsConfiguration.getConnection().jetStreamManagement();
            AccountStatistics stats = jsm.getAccountStatistics();
            ApiStats api = stats.getApi();

            String serverId = natsConfiguration.getConnection().getServerInfo().getServerId();

            NatsJetstreamHealthResponse response = NatsJetstreamHealthResponse.builder()
                    .memory(stats.getMemory())
                    .storage(stats.getStorage())
                    .reservedMemory(stats.getReservedMemory())
                    .reservedStorage(stats.getReservedStorage())
                    .streams(stats.getStreams())
                    .consumers(stats.getConsumers())
                    .limits(Map.of())
                    .api(NatsJetstreamHealthResponse.ApiStats.builder()
                            .level(api.getLevel())
                            .total(api.getTotalApiRequests())
                            .errors(api.getErrorCount())
                            .build())
                    .serverId(serverId)
                    .now(Instant.now().toString())
                    .build();

            logger.debug("GET /api/nats/health - success, server_id={}", serverId);
            return ResponseEntity.ok(response);
        } catch (IOException | JetStreamApiException e) {
            logger.error("GET /api/nats/health - failed to fetch JetStream stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @Operation(summary = "Stream consumer status", description = "Returns pending, ack-pending, and redelivered counts for every consumer on the given stream.")
    @GetMapping("/stream/{streamName}/status")
    public ResponseEntity<StreamStatusResponse> streamStatus(@PathVariable String streamName) {
        logger.debug("GET /api/nats/stream/{}/status", streamName);
        try {
            JetStreamManagement jsm = natsConfiguration.getConnection().jetStreamManagement();
            List<String> consumerNames = jsm.getConsumerNames(streamName);
            List<StreamStatusResponse.ConsumerStatus> statuses = new ArrayList<>();
            for (String consumerName : consumerNames) {
                ConsumerInfo info = jsm.getConsumerInfo(streamName, consumerName);
                statuses.add(StreamStatusResponse.ConsumerStatus.builder()
                        .consumer(consumerName)
                        .pending(info.getNumPending())
                        .ackPending(info.getNumAckPending())
                        .redelivered(info.getRedelivered())
                        .build());
            }
            return ResponseEntity.ok(StreamStatusResponse.builder()
                    .stream(streamName)
                    .consumers(statuses)
                    .build());
        } catch (JetStreamApiException e) {
            logger.warn("GET /api/nats/stream/{}/status - stream not found or NATS error: {}", streamName, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            logger.error("GET /api/nats/stream/{}/status - IO error: {}", streamName, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
