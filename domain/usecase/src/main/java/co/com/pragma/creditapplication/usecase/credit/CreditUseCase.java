package co.com.pragma.creditapplication.usecase.credit;

import co.com.pragma.creditapplication.model.client.gateways.ClientFeign;
import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.model.status.Status;
import co.com.pragma.creditapplication.model.status.gateways.StatusRepository;
import co.com.pragma.creditapplication.usecase.exception.NotFoundException;
import co.com.pragma.creditapplication.usecase.exception.SelfServiceViolationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreditUseCase {

    private final CreditApplicationRepository creditApplicationRepository;

    private final LoanTypeRepository loanTypeRepository;

    private final StatusRepository statusRepository;

    private final ClientFeign clientFeign;

    public Mono<String> createCreditApplication(CreditApplication creditApplication) {
        return validateLoanType(creditApplication.getLoanTypeId())
                .then(Mono.defer(() -> validateUser(creditApplication.getDocNumberClient(), creditApplication.getIdClient())))
                .flatMap(email -> {
                    creditApplication.setEmailClient(email);
                    return getPendingStatusId();
                })
                .flatMap(statusId -> {
                    creditApplication.setStatusId(statusId);
                    return creditApplicationRepository.save(creditApplication);
                })
                .thenReturn("Credit application successfully created.");
    }

    private Mono<Void> validateLoanType(Long loanTypeId) {
        return loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Loan type not found")))
                .then();
    }

    private Mono<String> validateUser(String docNumberClient, Long idClient) {
        return clientFeign.findByDocNumberClient(docNumberClient)
                .flatMap(userExist -> {
                    if (!userExist.found()) {
                        return Mono.error(new NotFoundException("User not found"));
                    }

                    if (!userExist.id().equals(idClient)) {
                        return Mono.error(new SelfServiceViolationException("Only the holder can create the credit application."));
                    }

                    return Mono.just(userExist.email());
                });
    }

    private Mono<Long> getPendingStatusId() {
        return statusRepository.findByName(LoanStatusEnum.PENDING_REVIEW.getName())
                .switchIfEmpty(Mono.error(new NotFoundException("Status not found")))
                .map(Status::getId);
    }

}
