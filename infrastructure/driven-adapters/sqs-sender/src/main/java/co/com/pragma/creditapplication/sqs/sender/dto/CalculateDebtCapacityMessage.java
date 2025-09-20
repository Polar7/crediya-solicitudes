package co.com.pragma.creditapplication.sqs.sender.dto;

import co.com.pragma.creditapplication.model.creditapplication.NewApplicationInformation;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditsApproved;

import java.math.BigDecimal;
import java.util.List;

public record CalculateDebtCapacityMessage(List<SelectCreditsApproved> credits,
                                           BigDecimal salary,
                                           NewApplicationInformation newApplicationInformation) {
}
