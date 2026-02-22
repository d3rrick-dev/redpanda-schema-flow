package com.d3rrick.kafkawithavro.app.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.*;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractiveQueryService {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    /**
     * Gets the total count for a specific domain from the KeyValueStore.
     */
    public Long getDomainCount(String domain) {
        return getKeyValueStore()
                .map(store -> store.get(domain))
                .orElse(0L);
    }

    /**
     * Fetches only the windows belonging to the specific domain.
     */
    public Map<String, Long> getDomainVelocity(String domain) {
        Map<String, Long> results = new LinkedHashMap<>();

        getWindowStore().ifPresent(store -> {
            var now = Instant.now();
            var from = now.minus(Duration.ofMinutes(15)); // Expanded range for visibility

            try (WindowStoreIterator<Long> iterator = store.fetch(domain, from, now)) {
                while (iterator.hasNext()) {
                    var next = iterator.next();
                    var timeLabel = Instant.ofEpochMilli(next.key).toString();
                    results.put(timeLabel, next.value);
                }
            }
        });

        return results;
    }

    // --- Helper Methods to safely access Stores ---

    private Optional<ReadOnlyKeyValueStore<String, Long>> getKeyValueStore() {
        return getStore("domain-counts-store", QueryableStoreTypes.keyValueStore());
    }

    private Optional<ReadOnlyWindowStore<String, Long>> getWindowStore() {
        return getStore("domain-velocity-store", QueryableStoreTypes.windowStore());
    }

    private <T> Optional<T> getStore(String storeName, QueryableStoreType<T> queryableStoreType) {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

        // Ensure Streams are actually RUNNING before querying
        if (kafkaStreams == null || kafkaStreams.state() != KafkaStreams.State.RUNNING) {
            log.warn("Kafka Streams is not in RUNNING state. Current state: {}",
                    kafkaStreams != null ? kafkaStreams.state() : "NULL");
            return Optional.empty();
        }

        try {
            return Optional.of(kafkaStreams.store(StoreQueryParameters.fromNameAndType(storeName, queryableStoreType)));
        } catch (Exception e) {
            log.error("Unable to retrieve state store: {}", storeName, e);
            return Optional.empty();
        }
    }
}