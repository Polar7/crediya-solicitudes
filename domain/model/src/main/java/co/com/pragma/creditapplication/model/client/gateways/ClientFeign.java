package co.com.pragma.creditapplication.model.client.gateways;

import co.com.pragma.creditapplication.model.client.ClientInfo;
import co.com.pragma.creditapplication.model.client.ValidatedClient;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ClientFeign {

    Mono<ValidatedClient> findByDocNumberClient(String docNumberClient);

    Mono<List<ClientInfo>> findClientsByEmails(List<String> emails);

}
