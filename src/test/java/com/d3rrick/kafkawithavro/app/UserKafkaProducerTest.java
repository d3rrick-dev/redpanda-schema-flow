package com.d3rrick.kafkawithavro.app;


import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.producer.UserKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, User> kafkaTemplate;

    @InjectMocks
    private UserKafkaProducer producer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "topic", "users-topic");
    }

    @Test
    void shouldSendUserSuccessfully() {
        var user = User.newBuilder().setId("1").setName("Derrick").setEmail("d@test.com").build();
        producer.sendUser(user);
        verify(kafkaTemplate, times(1)).send("users-topic", "1", user);
    }
}