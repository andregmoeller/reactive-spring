package com.github.andregmoeller.reactivedata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReactiveDataApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ReactiveDataApplication.class, args);
		Thread.sleep(1000 * 5);
	}
}

@Component
class DataInitializer implements ApplicationRunner {
    private final static Log log = LogFactory.getLog(DataInitializer.class);
    private final ReservationRepository reservationRepository;

    DataInitializer(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Flux<String> names = Flux.just("Pete", "Julie", "Josh", "Marcin", "Phil");
        Flux<Reservation> reservationFlux = names.map(name -> new Reservation(null, name));
        Flux<Reservation> saveFlux = reservationFlux.flatMap(reservationRepository::save);
        saveFlux.subscribe(new Subscriber<Reservation>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.info("onSubscribe( " + s.toString() + " )");
                s.request(10);
            }

            @Override
            public void onNext(Reservation reservation) {
                log.info("new reservation: " + reservation.toString());
            }

            @Override
            public void onError(Throwable t) {
                log.info("ooops!");
                log.info(t.toString());
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }
        });
    }
}

interface ReservationRepository extends ReactiveMongoRepository<Reservation, String> {
    Flux<Reservation> findByReservationName(String rn);
}

@Document
class Reservation {
    @Id
    private String id;

    private String reservationName;

    public Reservation(String id, String reservationName) {
        this.id = id;
        this.reservationName = reservationName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReservationName() {
        return reservationName;
    }

    public void setReservationName(String reservationName) {
        this.reservationName = reservationName;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", reservationName='" + reservationName + '\'' +
                '}';
    }
}