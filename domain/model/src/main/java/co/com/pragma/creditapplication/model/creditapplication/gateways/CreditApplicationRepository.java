package co.com.pragma.creditapplication.model.creditapplication.gateways;

import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditsApproved;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {

    Mono<CreditApplication> findById(Long id);

    Mono<CreditApplication> save(CreditApplication creditApplication);

    Mono<Integer> updateStatus(Long id, String statusName);

    Flux<SelectCreditApplication> findAllPendingByFiltersPaged(String emailClient, String loanTypeName, int page, int size);

    Mono<Long> countAllPendingByFilters(String emailClient, String loanTypeName);

    Flux<SelectCreditsApproved> findAllCreditsApprovedByClient(String emailClient);

}
