package com.github.andregmoeller.producer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@SpringBootApplication
@EnableBinding(Source.class)
public class ProducerApplication {
	@Bean
	ApplicationRunner producer(Source src) {
		return args -> {
			IntStream.range(0, 10).forEach(i -> {
				Message<String> msg = MessageBuilder.withPayload("Hello #" + i).build();
				src.output().send(msg);
			});
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}
}
