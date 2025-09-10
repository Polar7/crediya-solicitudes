package co.com.pragma.creditapplication.r2dbc.crud;

import co.com.pragma.creditapplication.r2dbc.entity.CreditApplicationEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CreditApplicationReactiveRepository extends R2dbcRepository<CreditApplicationEntity, Long> {

    @Modifying
    @Query("""
            UPDATE solicitud
            SET id_estado = s.id_estado
            FROM estados s
            WHERE solicitud.id_solicitud = :idSolicitud
              AND s.nombre = :statusName
            """)
    Mono<Integer> updateStatusBySolicitudIdAndStatusDescription(Long idCreditApplication, String statusName);

}
