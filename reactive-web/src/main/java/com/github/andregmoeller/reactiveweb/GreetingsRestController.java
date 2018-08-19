package com.github.andregmoeller.reactiveweb;

import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GreetingsRestController {
    @GetMapping("/greetings")
    Publisher<Greeting> greetings() {
        return Mono.just(new Greeting("Hello, world!"));
    }
}
