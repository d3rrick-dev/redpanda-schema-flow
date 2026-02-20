package com.d3rrick.kafkawithavro.app;

import com.d3rrick.kafkawithavro.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserConsumerServiceTest {

    private final UserConsumerService consumer = new UserConsumerService();

    @Test
    void shouldProcessUserObject() {
        var user = User.newBuilder().setId("100").setName("Tester").setEmail("t@t.com").build();
        assertDoesNotThrow(() -> consumer.consume(user));
    }
}