package com.example.kafkaOrderTesting.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.kafkaOrderTesting.AbstractKafkaIntegrationTest;

public class KafkaTestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		var kafka = AbstractKafkaIntegrationTest.kafkaContainer();
		kafka.start();
		TestPropertyValues.of("spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers())
				.applyTo(applicationContext.getEnvironment());
	}
}
