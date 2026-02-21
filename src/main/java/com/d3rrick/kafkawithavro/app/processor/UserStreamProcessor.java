package com.d3rrick.kafkawithavro.app.processor;

import com.d3rrick.kafkawithavro.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
@Slf4j
public class UserStreamProcessor {

    @Bean
    public KStream<String, User> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, User> userKStream = streamsBuilder.stream("users-topic");
        userKStream
                .mapValues(user -> {
                    user.setName(user.getName().toUpperCase());
                    log.info("Stream processed user: {}", user.getName());
                    return user;
                }).to("processed-users-topic");
        return userKStream;
    }
}