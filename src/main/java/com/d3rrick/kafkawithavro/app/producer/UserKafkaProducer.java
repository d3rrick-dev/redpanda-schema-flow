package com.d3rrick.kafkawithavro.app.producer;

import com.d3rrick.kafkawithavro.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserKafkaProducer {

    @Value("${spring.kafka.topics.users}")
    private String topic;

    private final KafkaTemplate<String, User> kafkaTemplate;

    public void sendUser(User user) {
        kafkaTemplate.send(topic, user.getId(), user);
    }
}
