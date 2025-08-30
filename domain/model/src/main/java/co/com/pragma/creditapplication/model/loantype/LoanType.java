package co.com.pragma.creditapplication.model.loantype;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {

    private Long id;

    private String description;

    private BigDecimal amountMin;

    private BigDecimal amountMax;

    private Double interestRate;

}
