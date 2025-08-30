package co.com.pragma.creditapplication.model.loantype.gateways;

import co.com.pragma.creditapplication.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {

    Mono<LoanType> findById(Long id);

}
