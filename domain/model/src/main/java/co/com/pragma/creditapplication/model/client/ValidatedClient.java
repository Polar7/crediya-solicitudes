package co.com.pragma.creditapplication.model.client;

import java.math.BigDecimal;

public record ValidatedClient(boolean found,
                              Long id,
                              String email,
                              BigDecimal salary) {
}
