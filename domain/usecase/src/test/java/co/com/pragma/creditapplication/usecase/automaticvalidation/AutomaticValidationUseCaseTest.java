package co.com.pragma.creditapplication.usecase.automaticvalidation;

import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import co.com.pragma.creditapplication.model.creditapplication.NewApplicationInformation;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditsApproved;
import co.com.pragma.creditapplication.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.pragma.creditapplication.model.creditapplication.gateways.ProducerMessagingBroker;
import co.com.pragma.creditapplication.model.status.LoanStatusEnum;
import co.com.pragma.creditapplication.usecase.exception.StatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomaticValidationUseCaseTest {

    @Mock
    private ProducerMessagingBroker producerMessagingBroker;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @InjectMocks
    private AutomaticValidationUseCase automaticValidationUseCase;

    @Test
    void initFlowAutomaticValidation_shouldCallRepositoryAndSendMessage() {
        String email = "client@test.com";
        BigDecimal salary = new BigDecimal("5000");
        NewApplicationInformation info = new NewApplicationInformation(1L, BigDecimal.TEN, 12, 0.05);

        List<SelectCreditsApproved> approvedList = List.of(new SelectCreditsApproved( new BigDecimal("1000"), 12, 20.0));

        when(creditApplicationRepository.findAllCreditsApprovedByClient(email))
                .thenReturn(Flux.fromIterable(approvedList));

        when(producerMessagingBroker.sendCalculateDebtCapacity(anyList(), eq(salary), eq(info)))
                .thenReturn(Mono.empty());

        StepVerifier.create(automaticValidationUseCase.initFlowAutomaticValidation(email, salary, info))
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).findAllCreditsApprovedByClient(email);
        verify(producerMessagingBroker, times(1)).sendCalculateDebtCapacity(eq(approvedList), eq(salary), eq(info));
    }

    @Test
    void initFlowAutomaticValidation_shouldHandleNoApprovedCredits() {
        String email = "client@test.com";
        BigDecimal salary = new BigDecimal("5000");
        NewApplicationInformation info = new NewApplicationInformation(1L, BigDecimal.TEN, 12, 0.05);

        when(creditApplicationRepository.findAllCreditsApprovedByClient(email))
                .thenReturn(Flux.empty());

        when(producerMessagingBroker.sendCalculateDebtCapacity(anyList(), eq(salary), eq(info)))
                .thenReturn(Mono.empty());

        StepVerifier.create(automaticValidationUseCase.initFlowAutomaticValidation(email, salary, info))
                .verifyComplete();

        verify(producerMessagingBroker, times(1)).sendCalculateDebtCapacity(eq(Collections.emptyList()), eq(salary), eq(info));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDIENTE", "OTRO_ESTADO", "APROBAADO"})
    void approveRejectStatus_shouldThrowExceptionForInvalidStatus(String invalidStatus) {
        Long id = 1L;

        StepVerifier.create(automaticValidationUseCase.approveRejectAutomaticApplicationStatus(id, invalidStatus, Collections.emptyList()))
                .expectErrorMatches(e -> e instanceof StatusException &&
                        e.getMessage().contains("Only APPROVED, REJECTED and MANUAL_REVIEW are accepted."))
                .verify();

        verify(creditApplicationRepository, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void approveRejectStatus_shouldApproveAndSendMessages() {
        Long id = 1L;
        String status = LoanStatusEnum.APPROVED.getName();
        CreditApplication editedApp = CreditApplication.builder()
                .id(id).emailClient("test@app.com").amount(new BigDecimal("10000")).build();

        when(creditApplicationRepository.updateStatus(id, status))
                .thenReturn(Mono.just(1));

        when(creditApplicationRepository.findById(id))
                .thenReturn(Mono.just(editedApp));

        when(producerMessagingBroker.sendUpdateCreditApplication(anyLong(), anyString(), anyString(), anyList()))
                .thenReturn(Mono.empty());
        when(producerMessagingBroker.sendMetricCreditApproved(any(BigDecimal.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(automaticValidationUseCase.approveRejectAutomaticApplicationStatus(id, status, Collections.emptyList()))
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).updateStatus(id, status);
        verify(creditApplicationRepository, times(1)).findById(id);
        verify(producerMessagingBroker, times(1)).sendUpdateCreditApplication(eq(id), eq(editedApp.getEmailClient()), eq(status), anyList());
        verify(producerMessagingBroker, times(1)).sendMetricCreditApproved(eq(editedApp.getAmount()));
    }

    @Test
    void approveRejectStatus_shouldRejectAndSkipMetricMessage() {
        Long id = 2L;
        String status = LoanStatusEnum.REJECTED.getName();
        CreditApplication editedApp = CreditApplication.builder()
                .id(id).emailClient("reject@app.com").amount(new BigDecimal("500")).build();

        when(creditApplicationRepository.updateStatus(id, status))
                .thenReturn(Mono.just(1));

        when(creditApplicationRepository.findById(id))
                .thenReturn(Mono.just(editedApp));

        when(producerMessagingBroker.sendUpdateCreditApplication(anyLong(), anyString(), anyString(), anyList()))
                .thenReturn(Mono.empty());

        StepVerifier.create(automaticValidationUseCase.approveRejectAutomaticApplicationStatus(id, status, Collections.emptyList()))
                .verifyComplete();

        verify(producerMessagingBroker, times(1)).sendUpdateCreditApplication(eq(id), eq(editedApp.getEmailClient()), eq(status), anyList());
        verify(producerMessagingBroker, never()).sendMetricCreditApproved(any(BigDecimal.class));
    }

}