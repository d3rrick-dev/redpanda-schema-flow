package com.d3rrick.kafkawithavro.app;

import com.d3rrick.kafkawithavro.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserConsumerService {

    @KafkaListener(topics = "${spring.kafka.topics.users}", groupId = "user-consumer-group")
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
}