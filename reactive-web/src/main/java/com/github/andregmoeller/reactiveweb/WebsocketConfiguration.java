package com.github.andregmoeller.reactiveweb;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Consumer;

@Configuration
public class WebsocketConfiguration {
    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    WebSocketHandler webSocketHandler() {
        return session -> {
            Publisher<WebSocketMessage> generate = Flux
                    .generate((Consumer<SynchronousSink<Greeting>>) sink -> sink.next(new Greeting("Hello @ " + Instant.now().toString())))
                    .map(g -> session.textMessage(g.getText()))
            .delayElements(Duration.ofSeconds(1))
            .doFinally(signalType -> System.out.println("goodbye"));
            return session.send(generate);
        };
    }

    @Bean
    HandlerMapping handlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Collections.singletonMap("/ws/hello", webSocketHandler()));
        mapping.setOrder(10);
        return mapping;
    }
}
