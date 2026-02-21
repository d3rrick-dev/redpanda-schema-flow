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
    void actualProductionFlow() {
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
    void retryandDLQScenario() {
        var poisonUser = User.newBuilder()
                .setId("fail-1")
                .setName("test")
                .setEmail("fail@test.com")
                .setPhoneNumber("90000")
                .build();

        producer.sendUser(poisonUser);

        // 1. Verify 4 total attempts (1 original + 3 retries)
        // timeout set to 20s because of the 5-second gaps in your logs
        verify(consumerService, timeout(20000).times(4)).consume(any(User.class));

        // 2. Verify DLT Handler call
        verify(consumerService, timeout(5000)).handleDlt(
                any(User.class),
                any(), // topic
                any() // offset/Partition
        );
    }
}