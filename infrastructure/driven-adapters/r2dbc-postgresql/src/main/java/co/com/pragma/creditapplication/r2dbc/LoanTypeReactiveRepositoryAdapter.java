package co.com.pragma.creditapplication.r2dbc;

import co.com.pragma.creditapplication.model.loantype.LoanType;
import co.com.pragma.creditapplication.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.creditapplication.r2dbc.crud.LoanTypeReactiveRepository;
import co.com.pragma.creditapplication.r2dbc.entity.LoanTypeEntity;
import co.com.pragma.creditapplication.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LoanTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
        Long,
        LoanTypeReactiveRepository
> implements LoanTypeRepository {

    public LoanTypeReactiveRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanType.class));
    }

}
