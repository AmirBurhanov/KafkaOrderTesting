package com.example.kafkaOrderTesting;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.kafkaOrderTesting.kafka.KafkaTopics;
import com.example.kafkaOrderTesting.model.OrderEvent;
import com.example.kafkaOrderTesting.model.OrderResult;
import com.example.kafkaOrderTesting.support.KafkaTestContainerInitializer;
import com.example.kafkaOrderTesting.support.ProcessedOrderResultSink;
import com.example.kafkaOrderTesting.support.TestKafkaListenersConfiguration;

@SpringBootTest(classes = KafkaOrderTestingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = KafkaTestContainerInitializer.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
@Import(TestKafkaListenersConfiguration.class)
public abstract class AbstractKafkaIntegrationTest {

	@Container
	private static final KafkaContainer KAFKA = new KafkaContainer(
			DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
			.withEmbeddedZookeeper()
			.withStartupTimeout(Duration.ofMinutes(3));

	@Autowired
	private KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;

	@Autowired
	private ProcessedOrderResultSink processedOrderResultSink;

	/** Для {@link com.example.kafkaOrderTesting.support.KafkaTestContainerInitializer} (другой пакет). */
	public static KafkaContainer kafkaContainer() {
		return KAFKA;
	}

	protected KafkaTemplate<String, OrderEvent> kafkaTemplate() {
		return orderEventKafkaTemplate;
	}

	protected ProcessedOrderResultSink orderResultSink() {
		return processedOrderResultSink;
	}

	protected void publishOrderCreated(OrderEvent event) {
		orderEventKafkaTemplate.send(KafkaTopics.ORDERS_CREATED, event.getOrderId(), event);
	}

	protected OrderResult awaitResultForOrder(String orderId) {
		await().atMost(Duration.ofSeconds(30))
				.pollInterval(Duration.ofMillis(150))
				.untilAsserted(() -> Assertions.assertTrue(processedOrderResultSink.findForOrder(orderId).isPresent()));
		return processedOrderResultSink.findForOrder(orderId).orElseThrow();
	}
}
