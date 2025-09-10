package co.com.pragma.creditapplication.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProcessApplicationDecisionDTO(@NotNull Long idCreditApplication,
                                            @NotEmpty String status) {
}
