package co.com.pragma.creditapplication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCreditApplicationDTO(@NotBlank String docNumberClient,
                                         @NotNull BigDecimal amountCredit,
                                         @NotNull Integer termCredit,
                                         @NotNull Long loanTypeId) {
}
