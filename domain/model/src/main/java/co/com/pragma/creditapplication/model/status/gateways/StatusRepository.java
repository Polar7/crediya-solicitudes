package co.com.pragma.creditapplication.model.status.gateways;

import co.com.pragma.creditapplication.model.status.Status;
import reactor.core.publisher.Mono;

public interface StatusRepository {

    Mono<Status> findByName(String name);

}
