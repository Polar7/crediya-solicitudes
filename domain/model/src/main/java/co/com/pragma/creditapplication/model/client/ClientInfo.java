package co.com.pragma.creditapplication.model.client;

import java.math.BigDecimal;

public record ClientInfo(String email,
                         String fullName,
                         BigDecimal salaryBase) {
}
