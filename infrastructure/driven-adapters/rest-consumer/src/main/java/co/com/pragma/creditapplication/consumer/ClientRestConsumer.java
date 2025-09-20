package co.com.pragma.creditapplication.consumer;

import co.com.pragma.creditapplication.consumer.dto.GenericResponseFeignDTO;
import co.com.pragma.creditapplication.model.client.ClientInfo;
import co.com.pragma.creditapplication.model.client.ValidatedClient;
import co.com.pragma.creditapplication.model.client.gateways.ClientFeign;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

 import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientRestConsumer implements ClientFeign {

    private final WebClient client;

    @CircuitBreaker(name = "findByDocNumberClient")
    @Override
    public Mono<ValidatedClient> findByDocNumberClient(String docNumberClient) {
        return Mono.deferContextual(contextView -> {
            String jwtToken = contextView.get("jwt");
            return client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/usuarios/{docNumber}")
                            .build(docNumberClient))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GenericResponseFeignDTO<ValidatedClient>>() {})
                    .map(dto -> new ValidatedClient(dto.detail().found(), dto.detail().id(), dto.detail().email(), dto.detail().salary()));
        });
    }

    @Override
    public Mono<List<ClientInfo>> findClientsByEmails(List<String> emails) {
        return Mono.deferContextual(contextView -> {
            String jwtToken = contextView.get("jwt");
            return client.post()
                    .uri("/api/v1/usuarios/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .bodyValue(emails)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GenericResponseFeignDTO<List<ClientInfo>>>() {})
                    .map(GenericResponseFeignDTO::detail);
        });
    }

}
