package com.github.andregmoeller.tweetservice;

import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
public class TweetServiceApplication {
    @Bean
    ApplicationRunner producer(TweetRepository repository) {
        return args -> {
            Author jonas = new Author("jboner");
            Author viktor = new Author("viktorklang");
            Author josh = new Author("starbuxman");
            Flux<Tweet> tweetFlux = Flux.just(
                    new Tweet("Woot, Konrad will be talking about #Enterprise #Integration done right!", viktor),
                    new Tweet("#scala implicits can easily be used to model Capabilities, but can they encode obligations easily? Easily as in: ergonomically.", viktor),
                    new Tweet("This is so cool! #akka", viktor),
                    new Tweet("Cross Data Center replication of event sourced #Akka actors is soon available (using #CRDTs, and more).", jonas),
                    new Tweet("a reminder: @SpringBoot lets you pair-program with the #Spring team.", josh)
            );

            repository
                    .deleteAll()
                    .thenMany(repository.saveAll(tweetFlux))
                    .thenMany(repository.findAll())
                    .subscribe(System.out::println);
        };
    }

    @Bean
    RouterFunction<ServerResponse> routes(TweetService tweetService) {
        return RouterFunctions.route(GET("/tweets"), req -> ok().body(tweetService.getAllTweets(), Tweet.class))
                .andRoute(GET("/hashtags"), req -> ok().body(tweetService.getAllHashtags(), HashTag.class));
    }

	public static void main(String[] args) {
		SpringApplication.run(TweetServiceApplication.class, args);
	}
}

@Configuration
class AkkaConfiguration {
    @Bean
    ActorMaterializer actorMaterializer() {
        ActorSystem actorSystem = ActorSystem.create("bootiful-akka-streams");
        return ActorMaterializer.create(actorSystem);
    }
}

@Service
@AllArgsConstructor
class TweetService {
    private final TweetRepository repository;
    private final ActorMaterializer actorMaterializer;

    Publisher<Tweet> getAllTweets() {
        return repository.findAll();
    }

    Publisher<HashTag> getAllHashtags() {
        return Source
                .fromPublisher(getAllTweets())
                .map(Tweet::getHashTags)
                .reduce(this::join)
                .mapConcat((Function<Set<HashTag>, ? extends Iterable<HashTag>>)  hashTags -> hashTags)
                .runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), actorMaterializer);
    }

    private <T> Set<T> join(Set<T> a, Set<T> b) {
        Set<T> set = new HashSet<>();
        set.addAll(a);
        set.addAll(b);
        return set;
    }
}

interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {
}

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class HashTag {
	@Id
	private String id;
}

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class Author {
	private String handle;
}

@Document
@Data
@AllArgsConstructor
class Tweet {
	@Id
	private String id;
	private String text;
	private Author author;

	Tweet(String text, Author author) {
	    this.text = text;
	    this.author = author;
    }

	public Set<HashTag> getHashTags() {
		return Arrays.stream(text.split(" "))
				.filter(t -> t.startsWith("#"))
				.map(word -> new HashTag(word.replaceAll("[^#\\w+]", "").toLowerCase()))
				.collect(Collectors.toSet());
	}
}
