package co.com.pragma.creditapplication.model.creditapplication.gateways;

import reactor.core.publisher.Mono;

public interface ProducerMessagingBroker {

    Mono<String> sendMessageUpdateCreditApplication(Long idApplication, String emailClient, String statusName);

}
