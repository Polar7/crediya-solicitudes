package co.com.pragma.creditapplication.model.creditapplication.gateways;

import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {

    Mono<CreditApplication> save(CreditApplication creditApplication);

    Flux<SelectCreditApplication> findAllPendingByFiltersPaged(String emailClient, String loanTypeName, int page, int size);

    Mono<Long> countAllPendingByFilters(String emailClient, String loanTypeName);

}
