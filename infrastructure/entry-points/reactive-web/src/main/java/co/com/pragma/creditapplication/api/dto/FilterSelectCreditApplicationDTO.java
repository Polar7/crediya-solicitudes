package co.com.pragma.creditapplication.api.dto;

import jakarta.validation.constraints.NotNull;

public record FilterSelectCreditApplicationDTO(String emailClient,
                                               String loanTypeName,
                                               @NotNull int page,
                                               @NotNull int size) {
}
