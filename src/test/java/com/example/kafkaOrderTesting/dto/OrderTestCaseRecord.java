package com.example.kafkaOrderTesting.dto;

import java.math.BigDecimal;

import com.example.kafkaOrderTesting.model.OrderStatus;

public record OrderTestCaseRecord(
		String orderId,
		String customerId,
		BigDecimal amount,
		OrderStatus expectedStatus,
		String description) {
}
