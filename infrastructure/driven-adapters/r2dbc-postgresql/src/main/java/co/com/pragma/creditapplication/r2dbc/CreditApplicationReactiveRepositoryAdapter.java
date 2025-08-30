package co.com.pragma.creditapplication.r2dbc;

import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.r2dbc.crud.CreditApplicationReactiveRepository;
import co.com.pragma.creditapplication.r2dbc.entity.CreditApplicationEntity;
import co.com.pragma.creditapplication.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CreditApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication,
        CreditApplicationEntity,
        Long,
        CreditApplicationReactiveRepository
> implements CreditApplicationRepository {

    public CreditApplicationReactiveRepositoryAdapter(CreditApplicationReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, CreditApplication.class));
    }

}
