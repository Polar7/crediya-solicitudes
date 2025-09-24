package co.com.pragma.creditapplication.usecase.automaticvalidation;

import co.com.pragma.creditapplication.model.creditapplication.NewApplicationInformation;
import co.com.pragma.creditapplication.model.creditapplication.PaymentPlan;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.creditapplication.gateways.ProducerMessagingBroker;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.usecase.exception.NotFoundException;
import co.com.pragma.creditapplication.usecase.exception.StatusException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class AutomaticValidationUseCase {

    private final ProducerMessagingBroker producerMessagingBroker;

    private final CreditApplicationRepository creditApplicationRepository;

    public Mono<Void> initFlowAutomaticValidation(String emailClient, BigDecimal salaryClient, NewApplicationInformation newApplicationInformation) {
        return creditApplicationRepository.findAllCreditsApprovedByClient(emailClient)
                .collectList()
                .flatMap(list -> producerMessagingBroker.sendCalculateDebtCapacity(list, salaryClient, newApplicationInformation));
    }

    public Mono<Void> approveRejectAutomaticApplicationStatus(Long idCreditApplication, String status, List<PaymentPlan> paymentPlans) {
        if (!status.equals(LoanStatusEnum.APPROVED.getName()) && !status.equals(LoanStatusEnum.REJECTED.getName()) && !status.equals(LoanStatusEnum.MANUAL_REVIEW.getName())) {
            return Mono.error(new StatusException("Only APPROVED, REJECTED and MANUAL_REVIEW are accepted."));
        }

        return creditApplicationRepository.updateStatus(idCreditApplication, status)
                .filter(rows -> rows > 0)
                .switchIfEmpty(Mono.error(new NotFoundException("Credit application not found")))
                .then(creditApplicationRepository.findById(idCreditApplication))
                .flatMap(creditApplicationEdited -> {
                    Mono<Void> updateMono = producerMessagingBroker.sendUpdateCreditApplication(creditApplicationEdited.getId(), creditApplicationEdited.getEmailClient(), status, paymentPlans);

                    Mono<Void> metricMono = Mono.empty();
                    if (LoanStatusEnum.APPROVED.getName().equals(status)) {
                        metricMono = producerMessagingBroker.sendMetricCreditApproved(creditApplicationEdited.getAmount());
                    }

                    return Mono.when(updateMono, metricMono);
                })
                .then();
    }

}
