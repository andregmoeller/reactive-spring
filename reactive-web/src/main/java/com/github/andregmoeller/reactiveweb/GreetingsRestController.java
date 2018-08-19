package com.github.andregmoeller.reactiveweb;

import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class GreetingsRestController {
    @GetMapping("/greetings")
    Publisher<Greeting> greetings() {
        Flux<Greeting> greetingFlux = Flux
                .<Greeting>generate(sink -> sink.next(new Greeting("Hello, world!")))
                .take(1000);
        return greetingFlux;
    }
}
