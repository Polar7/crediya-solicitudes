package co.com.pragma.creditapplication.model.creditapplication;

import java.math.BigDecimal;

public record PaymentPlan(Integer installmentNumber,
                          BigDecimal installmentAmount,
                          BigDecimal principalPayment,
                          BigDecimal interestPayment,
                          BigDecimal remainingBalance) {
}
