package workshop.redservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Flux;
import workshop.redservice.Vote.VoteBuilder;

@SpringBootApplication
public class RedServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedServiceApplication.class, args);
	}

}

@RestController
class RedServiceController {
	@Autowired
	DiscoveryClient discoveryClient;
	
	@Autowired
	WebClient.Builder webClientBuilder;
	
	@Value("${random.value}")
	String district;
	

	@GetMapping("/instances")
	List<?> getInstances() {
		return discoveryClient.getServices()
				.stream()
				.map(serviceId -> discoveryClient.getInstances(serviceId))
				.collect(Collectors.toList());
	}
	
	@CircuitBreaker(fallbackMethod = "emptyVote", name="default")
	@GetMapping(path = "/votes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Vote> getVotes() {
		return webClientBuilder.build()
				.get()
				.uri("http://vote-generator/random-numbers")
				.retrieve()
				.bodyToFlux(Integer.class)
				.map(count -> new VoteBuilder().count(count).district(district).unavailableMessage("").build());
	}
	
	@GetMapping(path = "/empty-vote", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Vote> emptyVote(Exception ex) {
		return Flux.just(new VoteBuilder().count(0).district(district).unavailableMessage("No Data avaiable").build());
	}
}


@Data
@Builder
class Vote {
	Integer count;
	String district;
	String unavailableMessage;
}


@Configuration
class Config {
	
	@Bean
	@LoadBalanced
	WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
	
}