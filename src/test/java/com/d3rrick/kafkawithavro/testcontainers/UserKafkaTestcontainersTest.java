package com.d3rrick.kafkawithavro.testcontainers;

import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.consumer.UserConsumerService;
import com.d3rrick.kafkawithavro.app.producer.UserKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("testcontainers")
class UserKafkaTestcontainersTest {

    @Container
    static RedpandaContainer redpanda = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.3.13");
    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);
        registry.add("spring.kafka.properties.schema.registry.url", redpanda::getSchemaRegistryAddress);
        registry.add("spring.kafka.streams.properties.schema.registry.url", redpanda::getSchemaRegistryAddress);
        registry.add("spring.kafka.streams.properties.state.dir",
                () -> "/tmp/kafka-streams/" + java.util.UUID.randomUUID());
    }
    @Autowired
    private UserKafkaProducer producer;

    @MockitoSpyBean
    private UserConsumerService consumerService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StreamsBuilderFactoryBean factoryBean;

    @BeforeEach
    void waitForStreams() {
        await()
                .atMost(Duration.ofSeconds(20))
                .until(() -> factoryBean.getKafkaStreams() != null &&
                        factoryBean.getKafkaStreams().state().isRunningOrRebalancing());
    }

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
        assertThat(user.getName().toUpperCase()).isEqualTo(capturedUser.getName());
        assertThat(capturedUser.getEmail()).contains("@redpanda.com");
    }

    @Test
    void testRetryAndDLQScenario() {
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
        verify(consumerService, timeout(10000)).handleDlt(
                any(User.class),    // Parameter 1: User
                any(String.class),  // Parameter 2: Topic Name Header
                any(String.class)   // Parameter 3: Exception Message Header
        );
    }

    @Test
    void testLeaderboardShouldIncrementAcrossMultipleEvents() {
        var domain = "gmail.com";
        int initialCount = getDomainCount(domain);

        for (int i = 0; i < 3; i++) {
            var user = User.newBuilder()
                    .setId("user-" + i)
                    .setName("User " + i)
                    .setEmail("test" + i + "@" + domain)
                    .setPhoneNumber("123456")
                    .build();
            producer.sendUser(user);
        }

        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    int currentCount = getDomainCount(domain);
                    assertThat(currentCount).isEqualTo(initialCount + 3);
                });
    }

    private int getDomainCount(String domain) {
        var url = "/analytics/domains/" + domain;
        var response = restTemplate.getForEntity(url, Integer.class);
        return response.getBody() != null ? response.getBody() : 0;
    }
}