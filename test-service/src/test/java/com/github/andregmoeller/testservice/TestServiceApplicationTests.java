package com.github.andregmoeller.testservice;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestServiceApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testPublisherService() {
		PublisherService publisherService = new PublisherService();

		StepVerifier.withVirtualTime(() -> publisherService.publish().take(10).collectList())
				.thenAwait(Duration.ofHours(10))
				.consumeNextWith(list -> Assert.assertEquals(10, list.size()))
				.verifyComplete();
	}

	@Test
	public void testWebCalls() {
		WebTestClient webClient = WebTestClient.bindToApplicationContext(context)
				.configureClient()
				.baseUrl("http://localhost:8080/")
				.build();

		webClient.get()
				.uri("/hi")
				.exchange()
				.expectStatus().isOk();
	}

}
