package com.d3rrick.kafkawithavro.testcontainers;

import com.d3rrick.kafkawithavro.BaseIntegrationTest;
import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.consumer.UserConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class UserKafkaTestcontainersTest extends BaseIntegrationTest {

    @MockitoSpyBean
    private UserConsumerService consumerService;

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
        verify(consumerService, timeout(30000).times(4)).consume(any(User.class));

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