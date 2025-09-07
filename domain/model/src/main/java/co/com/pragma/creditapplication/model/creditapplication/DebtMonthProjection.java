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
public class DebtMonthProjection {

    private String email;

    private BigDecimal totalDebt;

}
