package com.smartcart.payment;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentServiceApplicationTests {

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaServer;

	@PostConstruct
	public void printKafka() {
		System.out.println("Kafka Server = " + kafkaServer);
	}
	@Test
	void contextLoads() {
	}

}
