package com.d3rrick.kafkawithavro.integration;

import com.d3rrick.kafkawithavro.BaseIntegrationTest;
import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.consumer.UserConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class UserKafkaIT extends BaseIntegrationTest {
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
                receivedUser.getName().equals("IntegrationTest".toUpperCase())
        ));
    }
}