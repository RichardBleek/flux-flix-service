package com.rbleek.fluxflixservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@SpringBootApplication
public class FluxFlixServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FluxFlixServiceApplication.class, args);
	}

	@Bean
	RouterFunction<?> routes(FluxFlixService service){
		return route(RequestPredicates.GET("/movies"),
				request -> ServerResponse.ok().body(service.all(), Movie.class))
			.andRoute(RequestPredicates.GET("/movies/{id}"),
				request -> ServerResponse.ok().body(service.byId(request.pathVariable("id")), Movie.class))
			.andRoute(RequestPredicates.GET("movies/{id}/events"),
				request ->
					ServerResponse.ok()
						.contentType(MediaType.TEXT_EVENT_STREAM)
						.body(service.byId(request.pathVariable("id"))
						.flatMapMany(service::steamStreams), MovieEvent.class));
	}

	@Bean
	CommandLineRunner demo(MovieRepository movieRepository) {
		return args -> {
			movieRepository.deleteAll()
					.subscribe(null, null, () ->
						Stream.of("Aeon Flux", "Enter the Mono<Void>", "The Fluxinator",
					"Silence of the Lambdas", "Reactive Mongos on Plane", "Y Tu Mono Tambien",
					"Attack of the fluxxes", "Back to the Future")
					.map(name -> new Movie(UUID.randomUUID().toString(), name, randomGenre()))
					.forEach(m -> movieRepository.save(m).subscribe(System.out::println)));
		};
	}

	private String randomGenre() {
	    String [] genres = "horror,romcom,drama,action,docu".split(",");
	    return genres[new Random().nextInt(genres.length)];
	}
}

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
}

@Service
class FluxFlixService {

    private final MovieRepository movieRepository;

	public FluxFlixService(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	public Flux<MovieEvent> steamStreams(Movie movie) {

		Flux<Long> interval = Flux.interval(Duration.ofSeconds(1L));
		Flux<MovieEvent> events = Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randomUser())));

		return Flux.zip(interval, events)
				.map(Tuple2::getT2);
	}

	private String randomUser() {
		String[] users = "rbleek,dgeurts,pnederlof".split(",");
		return users[new Random().nextInt(users.length)];
	}

	public Flux<Movie> all() {
	    return movieRepository.findAll();
	}
	public Mono<Movie> byId(String id) {
	    return movieRepository.findById(id);
	}
}

// Traditional endpoint way, revisited by reactive endpoints introduced in Spring 5
//@RestController
//@RequestMapping("/movies")
//class MovieRestController {
//
//	private final FluxFlixService fluxFlixService;
//
//	public MovieRestController(FluxFlixService fluxFlixService) {
//		this.fluxFlixService = fluxFlixService;
//	}
//
//	@GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//	public Flux<MovieEvent> events(@PathVariable String id) {
//	    return fluxFlixService.byId(id)
//                .flatMapMany(fluxFlixService::steamStreams);
//	}
//
//	@GetMapping
//	public Flux<Movie> all() {
//		return fluxFlixService.all();
//	}
//
//	@GetMapping("/{id}")
//	public Mono<Movie> byId(@PathVariable String id) {
//		return fluxFlixService.byId(id);
//	}
//}

@Document
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Data
class Movie {

	@Id
	private String id;

	private String title, genre;
}

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
class MovieEvent {

	private Movie movie;
	private Date when;
	private String user;
}

