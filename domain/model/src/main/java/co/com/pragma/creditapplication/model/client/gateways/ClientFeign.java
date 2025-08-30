package co.com.pragma.creditapplication.model.client.gateways;

import co.com.pragma.creditapplication.model.client.ValidatedClient;
import reactor.core.publisher.Mono;

public interface ClientFeign {

    Mono<ValidatedClient> findByDocNumberClient(String docNumberClient);

}
