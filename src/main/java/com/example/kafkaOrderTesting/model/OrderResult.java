package com.example.kafkaOrderTesting.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderResult {

	private final String orderId;
	private final OrderStatus status;
	private final String reason;

	@JsonCreator
	public OrderResult(
			@JsonProperty("orderId") String orderId,
			@JsonProperty("status") OrderStatus status,
			@JsonProperty("reason") String reason) {
		this.orderId = Objects.requireNonNull(orderId, "orderId");
		this.status = Objects.requireNonNull(status, "status");
		this.reason = reason;
	}

	public String getOrderId() {
		return orderId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}
}
