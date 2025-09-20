package co.com.pragma.creditapplication.model.creditapplication;

import java.math.BigDecimal;

public record InitFlowAutomaticValidationEmailMessage(String emailClient,
                                                      BigDecimal salaryClient,
                                                      NewApplicationInformation newApplicationInformation) {
}
