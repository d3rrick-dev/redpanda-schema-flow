package com.d3rrick.kafkawithavro.app.processor;

import com.d3rrick.kafkawithavro.User;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Map;

@Configuration
@EnableKafkaStreams
@Slf4j
public class UserStreamProcessor {

    private SpecificAvroSerde<User> userSerde() {
        SpecificAvroSerde<User> serde = new SpecificAvroSerde<>();
        var config = Map.of(
                "schema.registry.url", "http://localhost:8081",
                "specific.avro.reader", "true"
        );
        serde.configure(config, false);
        return serde;
    }

    @Bean
    public KStream<String, User> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, User> userKStream = streamsBuilder.stream("users-topic",
                Consumed.with(Serdes.String(), userSerde()));

        // 1. Stateless Transformation
        KStream<String, User> processedStream = userKStream
//                .filter((key, user) -> !user.getName().equalsIgnoreCase("test"))
                .mapValues(user -> {
                    user.setName(user.getName().toUpperCase());
                    return user;
                });

        processedStream.to("processed-users-topic", Produced.with(Serdes.String(), userSerde()));

        // 2. Stateful Aggregation (The Leaderboard)
        processedStream
                .groupBy((key, user) -> {
                    var email = user.getEmail();
                    return email.substring(email.indexOf("@") + 1);
                }, Grouped.with(Serdes.String(), userSerde()))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("domain-counts-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))
                .toStream()
                .peek((domain, count) -> log.info("Domain Leaderboard -> {}: {}", domain, count));

        return userKStream;
    }
}