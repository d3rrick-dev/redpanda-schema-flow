package com.d3rrick.kafkawithavro;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

// This is also a smoke test
class KafkaWithAvroApplicationTests extends BaseIntegrationTest {
    @Test
    void contextLoads() {
        assertThat(Objects.requireNonNull(factoryBean.getKafkaStreams()).state().isRunningOrRebalancing()).isTrue();
    }
}
