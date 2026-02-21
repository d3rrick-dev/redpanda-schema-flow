package com.d3rrick.kafkawithavro.app.processor;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InteractiveQueryService {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public ReadOnlyKeyValueStore<String, Long> getDomainStore() {
        // 1. Get the underlying KafkaStreams instance
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

        // 2. Query the metadata to ensure the store is ready
        return kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                        "domain-counts-store", // Must match your Materialized.as() name
                        QueryableStoreTypes.keyValueStore()
                )
        );
    }
}