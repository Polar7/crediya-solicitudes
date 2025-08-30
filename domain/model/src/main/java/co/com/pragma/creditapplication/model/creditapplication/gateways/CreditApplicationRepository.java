package co.com.pragma.creditapplication.model.creditapplication.gateways;

import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {

    Mono<CreditApplication> save(CreditApplication creditApplication);

}
