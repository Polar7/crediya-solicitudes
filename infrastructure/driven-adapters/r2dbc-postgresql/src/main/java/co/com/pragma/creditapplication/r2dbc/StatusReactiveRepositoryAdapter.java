package co.com.pragma.creditapplication.r2dbc;

import co.com.pragma.creditapplication.model.status.Status;
import co.com.pragma.creditapplication.model.status.gateways.StatusRepository;
import co.com.pragma.creditapplication.r2dbc.crud.StatusReactiveRepository;
import co.com.pragma.creditapplication.r2dbc.entity.StatusEntity;
import co.com.pragma.creditapplication.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class StatusReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Status,
        StatusEntity,
        String,
        StatusReactiveRepository
> implements StatusRepository {

    public StatusReactiveRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Status.class));
    }

    @Override
    public Mono<Status> findByName(String name) {
        return repository.findByName(name).map(this::toEntity);
    }

}
