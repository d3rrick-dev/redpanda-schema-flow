package com.d3rrick.kafkawithavro.app.consumer;

import com.d3rrick.kafkawithavro.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserConsumerServiceDLQ {
//    DLQ deals with
//    Transient Failures: eg. database was down for 10 minutes (longer than your retries).
//    Poison Pills: The data is malformed or violates a business rule that no amount of retrying will fix.

    @KafkaListener(topics = "${spring.kafka.topics.dlq}", groupId = "user-consumer-group-dlq")
    public void consume(User user) {
        log.info("Starting consumption of DLQ User event");
        log.info("Received User Payload: [ID: {}, Name: {}, Email: {}, PhoneNumber: {}]",
                user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber());

        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Received DLQ User for ID: {}", user.getId());
        }
//        ignoring this
//        if (user.getName().startsWith("test")) {
//            log.error("Downstream error processing user record: {} !", user.getName());
//            throw new RuntimeException("Simulated downstream exception!");
//        }

        log.info("Successfully processed DLQ User event");

        // then for transient errors, I use the redrive approach,
        // push the exact same payload back into users-topic.
        // my main consumer will pick it up and processes it successfully.

        // for malformed errors
        // just do a manual cleanup (deleting or fixing code)

    }
}