package com.example.kafkaOrderTesting.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.example.kafkaOrderTesting.model.OrderEvent;
import com.example.kafkaOrderTesting.model.OrderResult;

@Configuration
public class KafkaJsonConfig {

	private static final String TRUSTED = "com.example.kafkaOrderTesting.model";

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroupId;

	@Bean
	public ProducerFactory<String, OrderEvent> orderEventProducerFactory() {
		return new DefaultKafkaProducerFactory<>(producerProps());
	}

	@Bean
	public ProducerFactory<String, OrderResult> orderResultProducerFactory() {
		return new DefaultKafkaProducerFactory<>(producerProps());
	}

	@Bean
	public KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate() {
		return new KafkaTemplate<>(orderEventProducerFactory());
	}

	@Bean
	public KafkaTemplate<String, OrderResult> orderResultKafkaTemplate() {
		return new KafkaTemplate<>(orderResultProducerFactory());
	}

	private Map<String, Object> producerProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		return props;
	}

	@Bean
	public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		JsonDeserializer<OrderEvent> deserializer = new JsonDeserializer<>(OrderEvent.class);
		deserializer.addTrustedPackages(TRUSTED);
		deserializer.setUseTypeHeaders(false);
		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderEventKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(orderEventConsumerFactory());
		return factory;
	}
}
