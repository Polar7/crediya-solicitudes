package co.com.pragma.creditapplication.usecase.credit;

import co.com.pragma.creditapplication.model.client.ClientInfo;
import co.com.pragma.creditapplication.model.client.ValidatedClient;
import co.com.pragma.creditapplication.model.client.gateways.ClientFeign;
import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.creditapplication.gateways.ProducerMessagingBroker;
import co.com.pragma.creditapplication.model.loantype.LoanType;
import co.com.pragma.creditapplication.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.model.status.Status;
import co.com.pragma.creditapplication.model.status.gateways.StatusRepository;
import co.com.pragma.creditapplication.usecase.exception.NotFoundException;
import co.com.pragma.creditapplication.usecase.exception.SelfServiceViolationException;
import co.com.pragma.creditapplication.usecase.exception.StatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditUseCaseTest {

    @InjectMocks
    CreditUseCase creditUseCase;

    @Mock
    CreditApplicationRepository creditApplicationRepository;

    @Mock
    LoanTypeRepository loanTypeRepository;

    @Mock
    StatusRepository statusRepository;

    @Mock
    ClientFeign clientFeign;

    @Mock
    ProducerMessagingBroker producerMessagingBroker;

    CreditApplication validCreditApplication;

    LoanType validLoanType;

    @BeforeEach
    void setup() {
        validCreditApplication = CreditApplication.builder()
                .idClient(1L)
                .docNumberClient("12345")
                .amount(new BigDecimal(500000))
                .term(12)
                .loanTypeId(1L)
                .build();

        validLoanType = new LoanType(1L, "Consumo", new BigDecimal(500000), new BigDecimal(50000000), 23.0);
    }

    @Nested
    class CreateCreditApplication {

        @Test
        void shouldCreateCreditApplication() {
            when(loanTypeRepository.findById(validCreditApplication.getLoanTypeId())).thenReturn(Mono.just(validLoanType));
            when(clientFeign.findByDocNumberClient(validCreditApplication.getDocNumberClient())).thenReturn(Mono.just(new ValidatedClient(true, validCreditApplication.getIdClient(), "email-valid@pragma.com")));
            when(statusRepository.findByName(LoanStatusEnum.PENDING_REVIEW.getName())).thenReturn(Mono.just(new Status(1L, "PENDIENTE", "Revision pendiente por parte del asesor")));
            when(creditApplicationRepository.save(any(CreditApplication.class))).thenReturn(Mono.just(validCreditApplication));

            StepVerifier.create(creditUseCase.createCreditApplication(validCreditApplication))
                    .expectNext("Credit application successfully created.")
                    .verifyComplete();

            verify(loanTypeRepository).findById(anyLong());
            verify(clientFeign).findByDocNumberClient(anyString());
            verify(statusRepository).findByName(anyString());
            verify(creditApplicationRepository).save(any(CreditApplication.class));
        }

        @Test
        void shouldThrowExceptionWhenLoanTypeNotExists() {
            CreditApplication invalidCreditApplication = validCreditApplication.toBuilder().loanTypeId(999L).build();

            when(loanTypeRepository.findById(invalidCreditApplication.getLoanTypeId())).thenReturn(Mono.empty());

            StepVerifier.create(creditUseCase.createCreditApplication(invalidCreditApplication))
                    .expectErrorMatches(throwable ->
                            throwable instanceof NotFoundException &&
                                    throwable.getMessage().equals("Loan type not found"))
                    .verify();

            verify(clientFeign, never()).findByDocNumberClient(anyString());
            verify(statusRepository, never()).findByName(anyString());
            verify(creditApplicationRepository, never()).save(any(CreditApplication.class));
        }

        @Test
        void shouldThrowExceptionWhenUserNotExists() {
            when(loanTypeRepository.findById(validCreditApplication.getLoanTypeId())).thenReturn(Mono.just(validLoanType));
            when(clientFeign.findByDocNumberClient(validCreditApplication.getDocNumberClient())).thenReturn(Mono.just(new ValidatedClient(false, null, null)));

            StepVerifier.create(creditUseCase.createCreditApplication(validCreditApplication))
                    .expectErrorMatches(throwable ->
                            throwable instanceof NotFoundException &&
                                    throwable.getMessage().equals("User not found"))
                    .verify();

            verify(loanTypeRepository).findById(anyLong());
            verify(clientFeign).findByDocNumberClient(anyString());
            verify(statusRepository, never()).findByName(anyString());
            verify(creditApplicationRepository, never()).save(any(CreditApplication.class));
        }

        @Test
        void shouldThrowExceptionWhenUserNotPermissions() {
            when(loanTypeRepository.findById(validCreditApplication.getLoanTypeId())).thenReturn(Mono.just(validLoanType));
            when(clientFeign.findByDocNumberClient(validCreditApplication.getDocNumberClient())).thenReturn(Mono.just(new ValidatedClient(true, 9999L, "email-valid@pragma.com")));

            StepVerifier.create(creditUseCase.createCreditApplication(validCreditApplication))
                    .expectErrorMatches(throwable ->
                            throwable instanceof SelfServiceViolationException &&
                                    throwable.getMessage().equals("Only the holder can create the credit application."))
                    .verify();

            verify(loanTypeRepository).findById(anyLong());
            verify(clientFeign).findByDocNumberClient(anyString());
            verify(statusRepository, never()).findByName(anyString());
            verify(creditApplicationRepository, never()).save(any(CreditApplication.class));
        }

        @Test
        void shouldThrowExceptionWhenPendingStatusNotFound() {
            when(loanTypeRepository.findById(anyLong())).thenReturn(Mono.just(new LoanType()));
            when(clientFeign.findByDocNumberClient(anyString())).thenReturn(Mono.just(new ValidatedClient(true, 1L, "test@email.com")));
            when(statusRepository.findByName(anyString())).thenReturn(Mono.empty());

            StepVerifier.create(creditUseCase.createCreditApplication(validCreditApplication))
                    .expectErrorMatches(throwable ->
                            throwable instanceof NotFoundException &&
                                    throwable.getMessage().equals("Status not found"))
                    .verify();

            verify(loanTypeRepository).findById(anyLong());
            verify(clientFeign).findByDocNumberClient(anyString());
            verify(statusRepository).findByName(anyString());
            verify(creditApplicationRepository, never()).save(any(CreditApplication.class));
        }

    }

    @Nested
    class GetAllCreditApplicationsPendingByFilters {

        @Test
        void shouldReturnEnrichedPagedResult() {
            String emailClient = null;
            String loanTypeName = null;
            int page = 0;
            int size = 10;

            SelectCreditApplication app1 = new SelectCreditApplication(
                    new BigDecimal("100000"), 240, "HIPOTECARIO", 5.0, "PENDIENTE",
                    null, "test1@example.com", null, null
            );
            SelectCreditApplication app2 = new SelectCreditApplication(
                    new BigDecimal("50000"), 180, "CONSUMO", 5.0, "PENDIENTE",
                    null, "test2@example.com", null, null
            );

            ClientInfo client1Info = new ClientInfo("test1@example.com", "John Doe", new BigDecimal("5000"));
            ClientInfo client2Info = new ClientInfo("test2@example.com", "Jane Smith", new BigDecimal("6000"));

            when(creditApplicationRepository.findAllPendingByFiltersPaged(emailClient, loanTypeName, page, size))
                    .thenReturn(Flux.just(app1, app2));
            when(clientFeign.findClientsByEmails(anyList()))
                    .thenReturn(Mono.just(List.of(client1Info, client2Info)));
            when(creditApplicationRepository.countAllPendingByFilters(emailClient, loanTypeName))
                    .thenReturn(Mono.just(2L));

            StepVerifier.create(creditUseCase.getAllCreditApplicationsPendingByFilters(emailClient, loanTypeName, page, size))
                    .expectNextMatches(pageResult -> {
                        if (pageResult.getTotalElements() != 2L) {
                            return false;
                        }

                        List<SelectCreditApplication> content = pageResult.getContent();
                        if (content.size() != 2) {
                            return false;
                        }

                        SelectCreditApplication result1 = content.stream()
                                .filter(app -> app.getEmailClient().equals("test1@example.com"))
                                .findFirst().orElseThrow();

                        if (!result1.getNameClient().equals("John Doe") ||
                                result1.getSalaryBaseClient().compareTo(new BigDecimal("5000")) != 0 ||
                                result1.getAmountMonthlyApplication() == null) {
                            return false;
                        }

                        SelectCreditApplication result2 = content.stream()
                                .filter(app -> app.getEmailClient().equals("test2@example.com"))
                                .findFirst().orElseThrow();

                        if (!result2.getNameClient().equals("Jane Smith") ||
                                result2.getSalaryBaseClient().compareTo(new BigDecimal("6000")) != 0 ||
                                result2.getAmountMonthlyApplication() == null) {
                            return false;
                        }

                        return true;
                    })
                    .verifyComplete();
        }

    }

    @Nested
    class ApproveRejectApplicationStatus {

        @Test
        void shouldThrowExceptionWhenStatusNotValid() {
            StepVerifier.create(creditUseCase.approveRejectManuallyApplicationStatus(1L, "NOT-EXIST"))
                    .expectErrorMatches(throwable ->
                            throwable instanceof StatusException &&
                                    throwable.getMessage().equals("Only APPROVED and REJECTED are accepted."))
                    .verify();

            verify(producerMessagingBroker, never()).sendMessageUpdateCreditApplication(anyLong(), anyString(), anyString());
        }

        @Test
        void shouldThrowExceptionWhenNotExistsCreditApplication() {
            when(creditApplicationRepository.updateStatus(anyLong(), anyString())).thenReturn(Mono.just(0));
            when(creditApplicationRepository.findById(anyLong())).thenReturn(Mono.empty());

            StepVerifier.create(creditUseCase.approveRejectManuallyApplicationStatus(1L, LoanStatusEnum.APPROVED.getName()))
                    .expectErrorMatches(throwable ->
                            throwable instanceof NotFoundException &&
                                    throwable.getMessage().equals("Credit application not found"))
                    .verify();

            verify(producerMessagingBroker, never()).sendMessageUpdateCreditApplication(anyLong(), anyString(), anyString());
        }

        @Test
        void shouldEnqueueSuccessfully() {
            when(creditApplicationRepository.updateStatus(anyLong(), anyString())).thenReturn(Mono.just(1));
            when(creditApplicationRepository.findById(anyLong())).thenReturn(Mono.just(validCreditApplication));
            when(producerMessagingBroker.sendMessageUpdateCreditApplication(validCreditApplication.getId(), validCreditApplication.getEmailClient(), LoanStatusEnum.APPROVED.getName())).thenReturn(Mono.just("OK"));

            StepVerifier.create(creditUseCase.approveRejectManuallyApplicationStatus(1L, LoanStatusEnum.APPROVED.getName()))
                    .expectNext("Update status successful")
                    .verifyComplete();

            verify(producerMessagingBroker).sendMessageUpdateCreditApplication(validCreditApplication.getId(), validCreditApplication.getEmailClient(), LoanStatusEnum.APPROVED.getName());
        }

    }

}