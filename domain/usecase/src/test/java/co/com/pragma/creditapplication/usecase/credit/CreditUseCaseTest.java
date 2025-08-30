package co.com.pragma.creditapplication.usecase.credit;

import co.com.pragma.creditapplication.model.client.ValidatedClient;
import co.com.pragma.creditapplication.model.client.gateways.ClientFeign;
import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.loantype.LoanType;
import co.com.pragma.creditapplication.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.model.status.Status;
import co.com.pragma.creditapplication.model.status.gateways.StatusRepository;
import co.com.pragma.creditapplication.usecase.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

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

    CreditApplication validCreditApplication;

    LoanType validLoanType;

    @BeforeEach
    void setup() {
        validCreditApplication = CreditApplication.builder()
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
            when(clientFeign.findByDocNumberClient(validCreditApplication.getDocNumberClient())).thenReturn(Mono.just(new ValidatedClient(true, "email-valid@pragma.com")));
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
            when(clientFeign.findByDocNumberClient(validCreditApplication.getDocNumberClient())).thenReturn(Mono.just(new ValidatedClient(false, null)));

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
        void shouldThrowExceptionWhenPendingStatusNotFound() {
            when(loanTypeRepository.findById(anyLong())).thenReturn(Mono.just(new LoanType()));
            when(clientFeign.findByDocNumberClient(anyString())).thenReturn(Mono.just(new ValidatedClient(true, "test@email.com")));
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

}