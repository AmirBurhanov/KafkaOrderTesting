package com.example.kafkaOrderTesting.support;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import com.example.kafkaOrderTesting.kafka.KafkaTopics;
import com.example.kafkaOrderTesting.model.OrderResult;

public class ProcessedOrderResultSink {

	private static final Logger log = LoggerFactory.getLogger(ProcessedOrderResultSink.class);

	private final List<OrderResult> results = new CopyOnWriteArrayList<>();

	@KafkaListener(
			topics = KafkaTopics.ORDERS_PROCESSED,
			groupId = "integration-test-processed-sink",
			containerFactory = "orderResultKafkaListenerContainerFactory")
	public void listen(OrderResult result) {
		log.debug("Test sink received {}", result);
		results.add(result);
	}

	public Optional<OrderResult> findForOrder(String orderId) {
		return results.stream().filter(r -> orderId.equals(r.getOrderId())).findFirst();
	}

	public void reset() {
		results.clear();
	}
}
