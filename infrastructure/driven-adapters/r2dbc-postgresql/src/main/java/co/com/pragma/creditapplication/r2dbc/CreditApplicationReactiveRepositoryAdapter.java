package co.com.pragma.creditapplication.r2dbc;

import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.r2dbc.crud.CreditApplicationReactiveRepository;
import co.com.pragma.creditapplication.r2dbc.entity.CreditApplicationEntity;
import co.com.pragma.creditapplication.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.creditapplication.r2dbc.queries.QueriesCreditApplication;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class CreditApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication,
        CreditApplicationEntity,
        Long,
        CreditApplicationReactiveRepository
> implements CreditApplicationRepository {

    private final DatabaseClient databaseClient;

    public CreditApplicationReactiveRepositoryAdapter(CreditApplicationReactiveRepository repository, ObjectMapper mapper, DatabaseClient databaseClient) {
        super(repository, mapper, d -> mapper.map(d, CreditApplication.class));
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<SelectCreditApplication> findAllPendingByFiltersPaged(String emailClient, String loanTypeName, int page, int size) {
        DatabaseClient.GenericExecuteSpec genericExecuteSpec =
                databaseClient.sql(QueriesCreditApplication.FIND_CREDIT_APPLICATIONS_PENDING_BY_FILTERS_PAGED);

        genericExecuteSpec = applyFiltersPendingApplications(genericExecuteSpec, emailClient, loanTypeName);

        return genericExecuteSpec
                .bind("size", size)
                .bind("offset", (long) page * size)
                .map((row, metadata) -> new SelectCreditApplication(
                        row.get("amount", BigDecimal.class),
                        row.get("term", Integer.class),
                        row.get("loanTypeName", String.class),
                        row.get("interestRate", Double.class),
                        row.get("statusName", String.class),
                        null,
                        row.get("emailClient", String.class),
                        null,
                        null
                ))
                .all();
    }

    @Override
    public Mono<Long> countAllPendingByFilters(String emailClient, String loanTypeName) {
        DatabaseClient.GenericExecuteSpec genericExecuteSpec =
                databaseClient.sql(QueriesCreditApplication.COUNT_FIND_CREDIT_APPLICATIONS_PENDING_BY_FILTERS_PAGED);

        genericExecuteSpec = applyFiltersPendingApplications(genericExecuteSpec, emailClient, loanTypeName);

        return genericExecuteSpec.map((row, metadata) -> row.get(0, Long.class)).one();
    }

    private DatabaseClient.GenericExecuteSpec applyFiltersPendingApplications(DatabaseClient.GenericExecuteSpec spec, String emailClient, String loanTypeName) {
        List<String> statusNames = List.of(
                LoanStatusEnum.REJECTED.getName(),
                LoanStatusEnum.PENDING_REVIEW.getName(),
                LoanStatusEnum.MANUAL_REVIEW.getName()
        );

        spec = spec.bind("statusNames", statusNames);

        if (emailClient != null) {
            spec = spec.bind("emailClient", emailClient);
        } else {
            spec = spec.bindNull("emailClient", String.class);
        }

        if (loanTypeName != null) {
            spec = spec.bind("loanTypeName", loanTypeName);
        } else {
            spec = spec.bindNull("loanTypeName", String.class);
        }

        return spec;
    }

}
