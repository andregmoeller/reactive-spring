package com.github.andregmoeller.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;

@EnableBinding(Sink.class)
@SpringBootApplication
public class ConsumerApplication {
	@Bean
	IntegrationFlow flow() {
		Flux<Date> dates = Flux.<Date>generate(sink -> sink.next(new Date()))
				.delayElements(Duration.ofSeconds(1));

		return IntegrationFlows.from(dates.map(date -> MessageBuilder.withPayload(date).build()))
				.handle((GenericHandler<Date>) (payload, headers) -> {
					System.out.println("the date is " + payload.toInstant().toString());
					return null;
				})
				.get();
	}

	@StreamListener
	public void process(@Input(Sink.INPUT) Flux<String> incomingStrings) {
		incomingStrings.map(String::toUpperCase)
				.subscribe(System.out::println);
	}

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}
}
