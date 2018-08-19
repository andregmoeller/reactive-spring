package com.github.andregmoeller.functionalspring;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;


@SpringBootApplication
public class FunctionalSpringApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder()
				.sources(FunctionalSpringApplication.class)
                .initializers((ApplicationContextInitializer<GenericApplicationContext>) gac -> {
                    if (Math.random() > .5) {
                        gac.registerBean(ApplicationRunner.class, () -> args1 -> System.out.println("hello, world"));
                    }
                })
                .run(args);
	}
}
