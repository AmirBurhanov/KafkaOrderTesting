package com.example.kafkaOrderTesting.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderEvent {

	private final String orderId;
	private final String customerId;
	private final BigDecimal amount;

	@JsonCreator
	public OrderEvent(
			@JsonProperty("orderId") String orderId,
			@JsonProperty("customerId") String customerId,
			@JsonProperty("amount") BigDecimal amount) {
		this.orderId = Objects.requireNonNull(orderId, "orderId");
		this.customerId = customerId;
		this.amount = amount;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
