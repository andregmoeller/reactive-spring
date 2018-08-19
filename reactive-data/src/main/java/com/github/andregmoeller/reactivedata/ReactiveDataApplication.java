package com.github.andregmoeller.reactivedata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        reservationRepository
                .deleteAll()
                .thenMany(
                        Flux.just("Pete", "Julie", "Josh", "Marcin", "Phil")
                                .map(name -> new Reservation(null, name))
                                .flatMap(reservationRepository::save)
                )
                .thenMany(reservationRepository.findAll())
                .subscribe(reservation -> log.info("new reservation: " + reservation.toString()));
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