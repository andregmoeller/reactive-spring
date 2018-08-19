package com.github.andregmoeller.reactiveweb;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfiguration {
    @Bean
    RouterFunction<ServerResponse> routes() {
        Publisher<Greeting> delayPublisher = Flux
                .<Greeting>generate(sink -> sink.next(new Greeting("Hello, world @ " + Instant.now().toString())))
                .delayElements(Duration.ofSeconds(1));

        return route(GET("/greetings"), r -> ServerResponse.ok().body(Flux.just("Hello, world!"), String.class))
                .andRoute(GET("/hi"), r ->  ServerResponse.ok().body(Flux.just("Hi"), String.class))
                .andRoute(GET("/sse"), r -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(delayPublisher, Greeting.class));
    }
}
