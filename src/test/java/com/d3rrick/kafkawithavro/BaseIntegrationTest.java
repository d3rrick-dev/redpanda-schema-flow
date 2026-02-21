package com.d3rrick.kafkawithavro;

import com.d3rrick.kafkawithavro.app.producer.UserKafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("testcontainers")
public abstract class BaseIntegrationTest {
    @Autowired
    protected UserKafkaProducer producer;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected StreamsBuilderFactoryBean factoryBean;

    @Container
    static RedpandaContainer redpanda = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.3.13");

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);
        registry.add("spring.kafka.properties.schema.registry.url", redpanda::getSchemaRegistryAddress);
        registry.add("spring.kafka.streams.properties.schema.registry.url", redpanda::getSchemaRegistryAddress);
        registry.add("spring.kafka.streams.properties.state.dir", () -> "/tmp/kafka-streams/" + java.util.UUID.randomUUID());
        registry.add("spring.kafka.streams.properties.commit.interval.ms", () -> "100");
    }
}
