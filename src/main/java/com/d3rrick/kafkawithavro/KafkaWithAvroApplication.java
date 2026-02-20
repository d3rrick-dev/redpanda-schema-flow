package com.d3rrick.kafkawithavro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableKafka
@SpringBootApplication
public class KafkaWithAvroApplication {

    static void main(String[] args) {
        SpringApplication.run(KafkaWithAvroApplication.class, args);
    }

}
