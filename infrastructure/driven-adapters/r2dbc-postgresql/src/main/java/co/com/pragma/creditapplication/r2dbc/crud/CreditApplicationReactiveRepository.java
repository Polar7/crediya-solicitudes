package co.com.pragma.creditapplication.r2dbc.crud;

import co.com.pragma.creditapplication.r2dbc.entity.CreditApplicationEntity;
import co.com.pragma.creditapplication.r2dbc.queries.QueriesCreditApplication;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CreditApplicationReactiveRepository extends R2dbcRepository<CreditApplicationEntity, Long> {

    @Query(QueriesCreditApplication.COUNT_FIND_CREDIT_APPLICATIONS_PENDING_BY_FILTERS_PAGED)
    Mono<Long> countAllPendingByFilters();

}
