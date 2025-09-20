package co.com.pragma.creditapplication.r2dbc.crud;

import co.com.pragma.creditapplication.r2dbc.entity.CreditApplicationEntity;
import co.com.pragma.creditapplication.r2dbc.queries.QueriesCreditApplication;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationReactiveRepository extends R2dbcRepository<CreditApplicationEntity, Long> {

    @Modifying
    @Query(QueriesCreditApplication.UPDATE_STATUS_BY_APPLICATIONID_AND_STATUSDESCRIPTION)
    Mono<Integer> updateStatusByApplicationIdAndStatusDescription(Long idCreditApplication, String statusName);

}
