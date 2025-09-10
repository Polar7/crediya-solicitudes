package co.com.pragma.creditapplication.model.creditapplication;
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
public class CreditApplication {

    private Long id;

    private Long idClient;

    private String docNumberClient;

    private BigDecimal amount;

    private Integer term;

    private Long loanTypeId;

    private String emailClient;

    private Long statusId;

}
