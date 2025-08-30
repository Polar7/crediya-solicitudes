package co.com.pragma.creditapplication.r2dbc.crud;

import co.com.pragma.creditapplication.r2dbc.entity.CreditApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationEntity, Long>, ReactiveQueryByExampleExecutor<CreditApplicationEntity> {

}
