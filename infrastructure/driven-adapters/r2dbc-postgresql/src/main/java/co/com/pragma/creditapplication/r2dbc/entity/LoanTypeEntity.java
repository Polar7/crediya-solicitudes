package co.com.pragma.creditapplication.r2dbc.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Entidad que moldea un Tipo de prestamo
 */
@Table("tipo_prestamo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoanTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id_tipo_prestamo")
    private Long id;

    @Column("nombre")
    private String description;

    @Column("monto_minimo")
    private BigDecimal amountMin;

    @Column("monto_maximo")
    private BigDecimal amountMax;

    @Column("tasa_interes")
    private Double interestRate;

}
