package com.example.kafkaOrderTesting.support;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.kafka.annotation.KafkaListener;

import com.example.kafkaOrderTesting.kafka.KafkaTopics;
import com.example.kafkaOrderTesting.model.OrderResult;

public class ProcessedOrderResultSink {

	private final List<OrderResult> results = new CopyOnWriteArrayList<>();

	@KafkaListener(
			topics = KafkaTopics.ORDERS_PROCESSED,
			groupId = "integration-test-processed-sink",
			containerFactory = "orderResultKafkaListenerContainerFactory")
	public void listen(OrderResult result) {
		results.add(result);
	}

	public Optional<OrderResult> findForOrder(String orderId) {
		return results.stream().filter(r -> orderId.equals(r.getOrderId())).findFirst();
	}

	public void reset() {
		results.clear();
	}
}
