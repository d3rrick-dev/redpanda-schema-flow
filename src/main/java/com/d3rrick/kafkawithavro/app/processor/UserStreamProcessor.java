package com.d3rrick.kafkawithavro.app.processor;

import com.d3rrick.kafkawithavro.User;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableKafkaStreams
@Slf4j
public class UserStreamProcessor {

    private SpecificAvroSerde<User> userSerde() {
        SpecificAvroSerde<User> serde = new SpecificAvroSerde<>();
        // In production, these should come from @Value or application.yml
        var config = Map.of(
                "schema.registry.url", "http://localhost:8081",
                "specific.avro.reader", "true"
        );
        serde.configure(config, false);
        return serde;
    }

    @Bean
    public KStream<String, User> userProcessingTopology(StreamsBuilder streamsBuilder) {
        // 1. Single Source: Read once, process many
        KStream<String, User> userKStream = streamsBuilder.stream("users-topic",
                Consumed.with(Serdes.String(), userSerde()));

        // 2. Stateless Transformation (Standardization)
        KStream<String, User> processedStream = userKStream
                .mapValues(user -> {
                    user.setName(user.getName().toString().toUpperCase());
                    return user;
                });

        // Push to downstream topic immediately
        processedStream.to("processed-users-topic", Produced.with(Serdes.String(), userSerde()));

        // 3. Grouping Logic (Re-used for both aggregations)
        KGroupedStream<String, User> groupedByDomain = processedStream.groupBy((key, user) -> {
            String email = user.getEmail().toString();
            return email.substring(email.indexOf("@") + 1);
        }, Grouped.with(Serdes.String(), userSerde()));

        // --- AGGREGATION A: Total Count (Global Leaderboard) ---
        groupedByDomain.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("domain-counts-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))
                .toStream()
                .peek((domain, count) -> log.info("🌍 Total Leaderboard -> {}: {}", domain, count));

        // --- AGGREGATION B: Windowed Count (Real-time Velocity) ---
        // Optimization: Added Suppression to emit only final window results
        groupedByDomain
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(30)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("domain-velocity-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded())) // ✅ Optimization for JD
                .toStream()
                .peek((windowedKey, count) -> log.info("🚀 1-Min Velocity for {}: {}", windowedKey.key(), count));

        return processedStream;
    }
}