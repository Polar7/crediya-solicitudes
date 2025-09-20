package co.com.pragma.creditapplication.sqs.sender.dto;

import co.com.pragma.creditapplication.model.creditapplication.PaymentPlan;

import java.util.List;

public record EmailUpdateCreditApplicationMessage(Long idApplication,
                                                  String emailClient,
                                                  String statusName,
                                                  List<PaymentPlan> paymentPlans) {
}
