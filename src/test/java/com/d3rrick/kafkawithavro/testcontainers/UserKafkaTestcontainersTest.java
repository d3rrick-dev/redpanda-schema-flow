package com.d3rrick.kafkawithavro.testcontainers;

import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.UserConsumerService;
import com.d3rrick.kafkawithavro.app.UserKafkaProducer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class UserKafkaTestcontainersTest {

    @Container
    static RedpandaContainer redpanda = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.3.13");
    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);
        registry.add("spring.kafka.properties.schema.registry.url", redpanda::getSchemaRegistryAddress);
    }

    @Autowired
    private UserKafkaProducer producer;

    @MockitoSpyBean
    private UserConsumerService consumerService;

    @Test
    void testActualProductionFlow() {
        var userCaptor = ArgumentCaptor.forClass(User.class);

        var user = User.newBuilder()
                .setId("ct-77")
                .setName("testContainer")
                .setEmail("testcontainer@redpanda.com")
                .setPhoneNumber("90000")
                .build();

        producer.sendUser(user);

        verify(consumerService, timeout(10000)).consume(userCaptor.capture());
        var capturedUser = userCaptor.getValue();
        assertThat(capturedUser).isNotNull();
        assertThat(user.getId()).isEqualTo(capturedUser.getId());
        assertThat(user.getName()).isEqualTo(capturedUser.getName());
        assertThat(capturedUser.getEmail()).contains("@redpanda.com");
    }

    @Test
    void testDLQScenario() {
        var poisonUser = User.newBuilder()
                .setId("fail-1")
                .setName("test")
                .setEmail("fail@test.com")
                .setPhoneNumber("90000")
                .build();

        producer.sendUser(poisonUser);
        // verify that consume method was called exactly 4 times (1st attempt + 3 retries)
        // count should be based on your FixedBackOff config (using default here)
        verify(consumerService, timeout(15000).times(4)).consume(any(User.class));
    }
}