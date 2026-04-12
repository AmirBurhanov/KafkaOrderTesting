package com.example.kafkaOrderTesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.kafkaOrderTesting.dto.OrderTestCaseRecord;
import com.example.kafkaOrderTesting.model.OrderEvent;
import com.example.kafkaOrderTesting.model.OrderResult;
import com.example.kafkaOrderTesting.model.OrderStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Все интеграционные сценарии в одном классе: один {@code @Testcontainers}-цикл на JVM (два класса с общим
 * статическим контейнером иногда приводили к повторному ContainerLaunch и падению Docker).
 */
@Execution(ExecutionMode.CONCURRENT)
class OrderPipelineIntegrationTest extends AbstractKafkaIntegrationTest {

	private static final Logger log = LoggerFactory.getLogger(OrderPipelineIntegrationTest.class);

	private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

	@Test
	@DisplayName("Контекст Spring и Kafka Testcontainers подняты")
	void spring_context_and_kafka_are_wired() {
		Assertions.assertNotNull(kafkaTemplate());
		Assertions.assertNotNull(orderResultSink());
		log.info("REPORT: spring_context_and_kafka_are_wired PASSED");
	}

	static Stream<Arguments> pipelineCases() {
		return Stream.of(
				Arguments.of("INT-MS-" + UUID.randomUUID(), new BigDecimal("42.5"), OrderStatus.ACCEPTED, "MethodSource: amount > 0"),
				Arguments.of("INT-MS-" + UUID.randomUUID(), BigDecimal.ZERO, OrderStatus.REJECTED, "MethodSource: amount is zero"),
				Arguments.of("INT-MS-" + UUID.randomUUID(), new BigDecimal("-3"), OrderStatus.REJECTED, "MethodSource: amount < 0"));
	}

	static Stream<Arguments> fromJsonFile() {
		try (InputStream in = OrderPipelineIntegrationTest.class.getResourceAsStream("/order-test-cases.json")) {
			Assertions.assertNotNull(in, "classpath:/order-test-cases.json must exist");
			List<OrderTestCaseRecord> cases = MAPPER.readValue(in, new TypeReference<List<OrderTestCaseRecord>>() {
			});
			return cases.stream()
					.map(c -> Arguments.of(c.orderId(), c.customerId(), c.amount(), c.expectedStatus(), c.description()));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	@DisplayName("Позитивный: amount > 0 даёт ACCEPTED в orders.processed")
	void positive_order_is_accepted() {
		String orderId = "POS-SINGLE-" + UUID.randomUUID();
		publishOrderCreated(new OrderEvent(orderId, "customer-pos", new BigDecimal("100.01")));

		OrderResult result = awaitResultForOrder(orderId);

		Assertions.assertEquals(OrderStatus.ACCEPTED, result.getStatus());
		Assertions.assertEquals(orderId, result.getOrderId());
		Assertions.assertNull(result.getReason());
		log.info("REPORT: positive_order_is_accepted PASSED for orderId={}, status={}", orderId, result.getStatus());
	}

	@Test
	@DisplayName("Негативный: amount <= 0 даёт REJECTED в orders.processed")
	void negative_order_is_rejected() {
		String orderId = "NEG-SINGLE-" + UUID.randomUUID();
		publishOrderCreated(new OrderEvent(orderId, "customer-neg", BigDecimal.ZERO));

		OrderResult result = awaitResultForOrder(orderId);

		Assertions.assertEquals(OrderStatus.REJECTED, result.getStatus());
		Assertions.assertEquals(orderId, result.getOrderId());
		Assertions.assertNotNull(result.getReason());
		log.info("REPORT: negative_order_is_rejected PASSED for orderId={}, status={}, reason={}",
				orderId, result.getStatus(), result.getReason());
	}

	@ParameterizedTest(name = "{3}")
	@MethodSource("pipelineCases")
	void parameterized_pipeline(String orderId, BigDecimal amount, OrderStatus expectedStatus, String description) {
		publishOrderCreated(new OrderEvent(orderId, "cust-ms", amount));

		OrderResult result = awaitResultForOrder(orderId);

		Assertions.assertEquals(expectedStatus, result.getStatus(), description);
		Assertions.assertEquals(orderId, result.getOrderId());
		log.info("REPORT: parameterized_pipeline PASSED case='{}' orderId={} expected={} actual={}",
				description, orderId, expectedStatus, result.getStatus());
	}

	@ParameterizedTest(name = "{4}")
	@MethodSource("fromJsonFile")
	@DisplayName("Data-driven: сценарии из order-test-cases.json")
	void json_driven_pipeline(String orderId, String customerId, BigDecimal amount, OrderStatus expectedStatus, String description) {
		publishOrderCreated(new OrderEvent(orderId, customerId, amount));

		OrderResult result = awaitResultForOrder(orderId);

		Assertions.assertEquals(expectedStatus, result.getStatus(), description);
		Assertions.assertEquals(orderId, result.getOrderId());
		log.info("REPORT: json_driven_pipeline PASSED description='{}' orderId={} status={}", description, orderId, result.getStatus());
	}
}
