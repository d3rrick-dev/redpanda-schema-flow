package com.d3rrick.kafkawithavro.integration;

import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.UserConsumerService;
import com.d3rrick.kafkawithavro.app.UserKafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"users-topic"})
@ActiveProfiles("test")
class UserKafkaIT {

    @Autowired
    private UserKafkaProducer producer;

    @MockitoSpyBean
    private UserConsumerService consumer;

    @Test
    void testFullAvroPipeline() {
        var user = User.newBuilder()
                .setId("999")
                .setName("IntegrationTest")
                .setEmail("bot@test.com")
                .build();

        producer.sendUser(user);
        verify(consumer, timeout(5000)).consume(argThat(receivedUser ->
                receivedUser.getName().equals("IntegrationTest")
        ));
    }
}