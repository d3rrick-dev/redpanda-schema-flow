//package com.d3rrick.kafkawithavro.app;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
//import org.springframework.kafka.listener.DefaultErrorHandler;
//import org.springframework.util.backoff.FixedBackOff;
//
//@Configuration
//public class KafkaErrorHandlerConfig {
//
//    @Bean
//    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
//        // 1. Where to send the failed message
//        var recoverer = new DeadLetterPublishingRecoverer(template);
//
//        // 2. Retry logic: 3 attempts, 1 second apart
//        var backOff = new FixedBackOff(1000L, 3);
//
//        return new DefaultErrorHandler(recoverer, backOff);
//    }
//}
