package com.example.kafkaOrderTesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaOrderTestingApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaOrderTestingApplication.class, args);
	}

}
