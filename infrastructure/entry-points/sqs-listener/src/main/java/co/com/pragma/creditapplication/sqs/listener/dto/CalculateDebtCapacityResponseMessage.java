package co.com.pragma.creditapplication.sqs.listener.dto;

import co.com.pragma.creditapplication.model.creditapplication.PaymentPlan;

import java.util.List;

public record CalculateDebtCapacityResponseMessage(Long idCreditApplication,
                                                   String statusName,
                                                   List<PaymentPlan> paymentPlans) {
}
