package co.com.pragma.creditapplication.model.creditapplication;

import java.math.BigDecimal;

public record NewApplicationInformation(Long idCreditApplication,
                                        BigDecimal amount,
                                        Integer term,
                                        Double interestRate) {

}
