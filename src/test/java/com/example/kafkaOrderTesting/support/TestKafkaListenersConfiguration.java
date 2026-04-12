package com.example.kafkaOrderTesting.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.example.kafkaOrderTesting.model.OrderResult;

@TestConfiguration
public class TestKafkaListenersConfiguration {

	private static final String TRUSTED = "com.example.kafkaOrderTesting.model";

	@Bean
	ConsumerFactory<String, OrderResult> testOrderResultConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
		Map<String, Object> props = new HashMap<>(
				KafkaTestUtils.consumerProps(bootstrapServers, "integration-test-processed-sink", "false"));
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
		props.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
		JsonDeserializer<OrderResult> deserializer = new JsonDeserializer<>(OrderResult.class);
		deserializer.addTrustedPackages(TRUSTED);
		deserializer.setUseTypeHeaders(false);
		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, OrderResult> orderResultKafkaListenerContainerFactory(
			ConsumerFactory<String, OrderResult> testOrderResultConsumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, OrderResult> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(testOrderResultConsumerFactory);
		return factory;
	}

	@Bean
	ProcessedOrderResultSink processedOrderResultSink() {
		return new ProcessedOrderResultSink();
	}
}
