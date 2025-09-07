package co.com.pragma.creditapplication.model.creditapplication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SelectCreditApplication {

    private BigDecimal amount;

    private Integer term;

    private String loanTypeName;

    private Double interestRate;

    private String statusName;

    private String nameClient;

    private String emailClient;

    private BigDecimal salaryBaseClient;

    private BigDecimal amountMonthlyApplication;

}
