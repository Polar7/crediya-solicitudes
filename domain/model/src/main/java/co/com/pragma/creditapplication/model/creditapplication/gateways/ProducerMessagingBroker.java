package co.com.pragma.creditapplication.model.creditapplication.gateways;

import co.com.pragma.creditapplication.model.creditapplication.NewApplicationInformation;
import co.com.pragma.creditapplication.model.creditapplication.PaymentPlan;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditsApproved;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface ProducerMessagingBroker {

    Mono<Void> sendUpdateCreditApplication(Long idApplication, String emailClient, String statusName, List<PaymentPlan> paymentPlans);

    Mono<Void> sendInitFlowAutomaticValidation(String emailClient, BigDecimal salaryClient, NewApplicationInformation newApplicationInformation);

    Mono<Void> sendCalculateDebtCapacity(List<SelectCreditsApproved> listCreditsApproved, BigDecimal salaryClient, NewApplicationInformation newApplicationInformation);

}
