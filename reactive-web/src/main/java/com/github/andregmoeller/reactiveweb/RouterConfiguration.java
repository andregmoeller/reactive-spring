package com.github.andregmoeller.reactiveweb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfiguration {
    @Bean
    RouterFunction<ServerResponse> routes() {
        return route(GET("/greetings").or(GET("/hello")),
                serverRequest -> ServerResponse.ok().body(Flux.just("Hello, world!"), String.class));
    }
}