package co.com.pragma.creditapplication.usecase.credit;

import co.com.pragma.creditapplication.model.client.ClientInfo;
import co.com.pragma.creditapplication.model.client.ValidatedClient;
import co.com.pragma.creditapplication.model.client.gateways.ClientFeign;
import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.NewApplicationInformation;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.creditapplication.gateways.ProducerMessagingBroker;
import co.com.pragma.creditapplication.model.loantype.LoanType;
import co.com.pragma.creditapplication.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.creditapplication.model.page.Page;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.model.status.Status;
import co.com.pragma.creditapplication.model.status.gateways.StatusRepository;
import co.com.pragma.creditapplication.usecase.exception.NotFoundException;
import co.com.pragma.creditapplication.usecase.exception.SelfServiceViolationException;
import co.com.pragma.creditapplication.usecase.exception.StatusException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CreditUseCase {

    private final CreditApplicationRepository creditApplicationRepository;

    private final LoanTypeRepository loanTypeRepository;

    private final StatusRepository statusRepository;

    private final ClientFeign clientFeign;

    private final ProducerMessagingBroker producerMessagingBroker;

    public Mono<String> createCreditApplication(CreditApplication creditApplication) {
        return validateLoanType(creditApplication.getLoanTypeId())
                .zipWhen(loanType -> validateUser(creditApplication.getDocNumberClient(), creditApplication.getIdClient()))
                .zipWhen(tuple -> getPendingStatusId())
                .flatMap(tuple -> {
                    LoanType loanType = tuple.getT1().getT1();
                    ValidatedClient validateClient = tuple.getT1().getT2();
                    Long statusId = tuple.getT2();

                    creditApplication.setEmailClient(validateClient.email());
                    creditApplication.setStatusId(statusId);

                    return creditApplicationRepository.save(creditApplication)
                            .flatMap(saved -> {
                                if (loanType.isAutomaticValidation()) {
                                    return producerMessagingBroker.sendInitFlowAutomaticValidation(saved.getEmailClient(),
                                            validateClient.salary(),
                                            new NewApplicationInformation(saved.getId(), saved.getAmount(), saved.getTerm(), loanType.getInterestRate())); //  Todo extraer a metodo
                                }
                                return Mono.empty();
                            });
                })
                .thenReturn("Credit application successfully created.");
    }

    public Mono<String> approveRejectManuallyApplicationStatus(Long idCreditApplication, String status) {
        if (!status.equals(LoanStatusEnum.APPROVED.getName()) && !status.equals(LoanStatusEnum.REJECTED.getName())) {
            return Mono.error(new StatusException("Only APPROVED and REJECTED are accepted."));
        }

        return creditApplicationRepository.updateStatus(idCreditApplication, status)
                .filter(rows -> rows > 0)
                .switchIfEmpty(Mono.error(new NotFoundException("Credit application not found")))
                .then(creditApplicationRepository.findById(idCreditApplication))
                .flatMap(creditApplicationEdited -> {
                    Mono<Void> updateMono = producerMessagingBroker.sendUpdateCreditApplication(creditApplicationEdited.getId(), creditApplicationEdited.getEmailClient(), status, null);

                    Mono<Void> metricMono = Mono.empty();
                    if (LoanStatusEnum.APPROVED.getName().equals(status) && creditApplicationEdited.getAmount() != null) {
                        metricMono = producerMessagingBroker.sendMetricCreditApproved(creditApplicationEdited.getAmount());
                    }

                    return Mono.when(updateMono, metricMono);
                })
                .thenReturn("Update status successful");
    }

    public Mono<Page<SelectCreditApplication>> getAllCreditApplicationsPendingByFilters(String emailClient, String loanTypeName, int page, int size) {
        Flux<SelectCreditApplication> listFoundFlux = creditApplicationRepository.findAllPendingByFiltersPaged(emailClient, loanTypeName, page, size);

        Mono<Map<String, ClientInfo>> clientsByEmailMono = listFoundFlux
                .map(SelectCreditApplication::getEmailClient)
                .distinct()
                .collectList()
                .flatMap(clientFeign::findClientsByEmails)
                .onErrorResume(throwable -> Mono.just(List.of()))
                .flatMapMany(Flux::fromIterable)
                .collectMap(ClientInfo::email);

        Mono<List<SelectCreditApplication>> enrichedContent = enrichContentSelectPendingApplications(listFoundFlux, clientsByEmailMono);

        Mono<Long> total = creditApplicationRepository.countAllPendingByFilters(emailClient, loanTypeName);

        return Mono.zip(enrichedContent, total)
                .map(tuple -> {
                    Page<SelectCreditApplication> pageResult = new Page<>();
                    pageResult.setContent(tuple.getT1());
                    pageResult.setTotalElements(tuple.getT2());
                    return pageResult;
                });
    }

    private Mono<LoanType> validateLoanType(Long loanTypeId) {
        return loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Loan type not found")));
    }

    private Mono<ValidatedClient> validateUser(String docNumberClient, Long idClient) {
        return clientFeign.findByDocNumberClient(docNumberClient)
                .flatMap(userExist -> {
                    if (!userExist.found()) {
                        return Mono.error(new NotFoundException("User not found"));
                    }

                    if (!userExist.id().equals(idClient)) {
                        return Mono.error(new SelfServiceViolationException("Only the holder can create the credit application."));
                    }

                    return Mono.just(userExist);
                });
    }

    private Mono<Long> getPendingStatusId() {
        return statusRepository.findByName(LoanStatusEnum.PENDING_REVIEW.getName())
                .switchIfEmpty(Mono.error(new NotFoundException("Status not found")))
                .map(Status::getId);
    }

    private BigDecimal calculateQuoteMonthCredit(BigDecimal amount, int term, double interestRate, String loanType) {
        BigDecimal monthlyInterestRate = BigDecimal.valueOf(interestRate)
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        if ("HIPOTECARIO".equalsIgnoreCase(loanType)) {
            BigDecimal base = BigDecimal.ONE.add(monthlyInterestRate);

            BigDecimal denominator = BigDecimal.ONE.subtract(
                    BigDecimal.ONE.divide(base.pow(term, new MathContext(10, RoundingMode.HALF_UP)), 10, RoundingMode.HALF_UP)
            );

            BigDecimal numerator = amount.multiply(monthlyInterestRate);

            return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal capitalAmortization = amount.divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP);
            BigDecimal monthlyInterest = amount.multiply(monthlyInterestRate);

            return capitalAmortization.add(monthlyInterest);
        }
    }

    private Mono<List<SelectCreditApplication>> enrichContentSelectPendingApplications(Flux<SelectCreditApplication> selectCreditApplicationFlux, Mono<Map<String, ClientInfo>> clientsByEmailMono) {
        return selectCreditApplicationFlux
                .collectList()
                .zipWith(clientsByEmailMono)
                .map(tuple -> {
                    List<SelectCreditApplication> applications = tuple.getT1();
                    Map<String, ClientInfo> clientsByEmail = tuple.getT2();

                    applications.parallelStream().forEach(application -> {
                        String email = application.getEmailClient();

                        ClientInfo client = clientsByEmail.get(email);
                        if (client != null) {
                            application.setNameClient(client.fullName());
                            application.setSalaryBaseClient(client.salaryBase());
                        }

                        application.setAmountMonthlyApplication(
                                calculateQuoteMonthCredit(application.getAmount(), application.getTerm(), application.getInterestRate(), application.getLoanTypeName())
                        );
                    });
                    return applications;
                });
    }

}
