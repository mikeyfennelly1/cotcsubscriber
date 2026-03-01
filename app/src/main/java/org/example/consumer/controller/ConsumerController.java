package org.example.consumer.controller;

import org.example.consumer.subscriber.*;
import org.example.consumer.subscriber.exception.InvalidSubscriptionTreePathFormatException;
import org.example.consumer.subscriber.exception.SubscriptionAlreadyExistsException;
import org.example.consumer.subscriber.exception.TreePathNotFoundException;
import org.example.consumer.subscriber.manager.tree.SubscriptionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumer/groups")
public class ConsumerController {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    private final SubscriptionSubsystemFacade subscriptionSubsystemFacade;

    @Autowired
    public ConsumerController(
            SubscriptionSubsystemFacade subscriptionSubsystemFacade
    ) {
        this.subscriptionSubsystemFacade = subscriptionSubsystemFacade;
    }

    @GetMapping("")
    public List<String> getAllGroups() {
        logger.debug("GET /api/consumer/groups - fetching active subscriptions");
        return subscriptionSubsystemFacade.readAllSubscriptions();
    }

    @PostMapping("")
    public ResponseEntity<?> createNewGroup(@RequestBody(required = false) Map<String, String> body) {
        logger.debug("POST /api/consumer/groups - body: {}", body);
        if (body == null || !body.containsKey("name") || body.get("name") == null || body.get("name").isBlank()) {
            logger.debug("POST /api/consumer/groups - rejected: missing or blank 'name' field");
            return ResponseEntity.badRequest().body(Map.of("error", "Request body must include a 'name' field."));
        }
        if (!body.containsKey("parent")) {
            logger.debug("POST /api/consumer/groups - rejected: missing 'parent' field");
            return ResponseEntity.badRequest().body(Map.of("error", "Request body must include a 'parent' field (set to null to create a root node)."));
        }
        try {
            subscriptionSubsystemFacade.createSubscription(body.get("name"), body.get("parent"));
        } catch (InvalidSubscriptionTreePathFormatException e) {
            logger.debug("POST /api/consumer/groups - rejected: invalid path format - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (TreePathNotFoundException e) {
            logger.debug("POST /api/consumer/groups - rejected: parent path not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SubscriptionAlreadyExistsException e) {
            logger.debug("POST /api/consumer/groups - rejected: subscription already exists - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
        logger.debug("POST /api/consumer/groups - created subscription name='{}' parent='{}'", body.get("name"), body.get("parent"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/children")
    public ResponseEntity<?> getChildGroups(@RequestParam String parent) {
        logger.debug("GET /api/consumer/groups/children - parent='{}'", parent);
        try {
            List<SubscriptionNode> childSubscriptions = subscriptionSubsystemFacade.getChildSubscriptions(parent);
            return ResponseEntity.ok(childSubscriptions);
        } catch (InvalidSubscriptionTreePathFormatException e) {
            logger.debug("GET /api/consumer/groups/children - rejected: invalid path format - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (TreePathNotFoundException e) {
            logger.debug("GET /api/consumer/groups/children - rejected: parent path not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
