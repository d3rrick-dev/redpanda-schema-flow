package com.d3rrick.kafkawithavro.app.consumer;

import com.d3rrick.kafkawithavro.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserConsumerService {

    @KafkaListener(topics = "${spring.kafka.topics.processed}", groupId = "user-consumer-group")
    @RetryableTopic(
            attempts = "4",                                 // Total attempts (1 original + 3 retries)
            backoff = @Backoff(delay = 1000),               // 1 sec (1 ms) delay between tries
            dltStrategy = DltStrategy.FAIL_ON_ERROR        // Send to DLT after all retries fail
    )
    public void consume(User user) {
        log.info("Starting consumption of User event");
        log.info("Received User Payload: [ID: {}, Name: {}, Email: {}, PhoneNumber: {}]",
                user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber());

        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Received User with missing name for ID: {}", user.getId());
        }

        if ("test".equalsIgnoreCase(user.getName())) {
            log.error("Downstream error processing user record: {} !", user.getName());
            throw new RuntimeException("Simulated downstream exception!");
        }

        log.info("Successfully processed User event");
    }

    @DltHandler
    public void handleDlt(
            User user,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String currentTopic, // The .DLT topic name
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exception  // Why it failed
    ) {
        log.error("Permanent Failure in topic: {}. Error: {}", currentTopic, exception);
    }
}