package com.example.kafkaOrderTesting.consumer;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.kafkaOrderTesting.kafka.KafkaTopics;
import com.example.kafkaOrderTesting.model.OrderEvent;
import com.example.kafkaOrderTesting.model.OrderResult;
import com.example.kafkaOrderTesting.model.OrderStatus;

@Component
public class OrderProcessingConsumer {

	private final KafkaTemplate<String, OrderResult> orderResultKafkaTemplate;

	public OrderProcessingConsumer(KafkaTemplate<String, OrderResult> orderResultKafkaTemplate) {
		this.orderResultKafkaTemplate = orderResultKafkaTemplate;
	}

	@KafkaListener(
			topics = KafkaTopics.ORDERS_CREATED,
			groupId = "${spring.kafka.consumer.group-id}",
			containerFactory = "orderEventKafkaListenerContainerFactory")
	public void onOrderCreated(OrderEvent event) {
		boolean valid = isAmountPositive(event.getAmount());
		OrderStatus status = valid ? OrderStatus.ACCEPTED : OrderStatus.REJECTED;
		String reason = valid ? null : "amount must be greater than 0";
		OrderResult result = new OrderResult(event.getOrderId(), status, reason);
		orderResultKafkaTemplate.send(KafkaTopics.ORDERS_PROCESSED, event.getOrderId(), result);
	}

	static boolean isAmountPositive(BigDecimal amount) {
		return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
	}
}
