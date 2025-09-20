package co.com.pragma.creditapplication.sqs.listener;

import co.com.pragma.creditapplication.sqs.listener.dto.CalculateDebtCapacityResponseMessage;
import co.com.pragma.creditapplication.sqs.listener.exception.SQSProcessorException;
import co.com.pragma.creditapplication.usecase.automaticvalidation.AutomaticValidationUseCase;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SQSProcessorCalculateDebtCapacityResponse implements Function<Message, Mono<Void>> {

    private final AutomaticValidationUseCase automaticValidationUseCase;

    private final Gson gson;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("LISTENER SQSProcessorCalculateDebtCapacityResponse");
        log.info(message.body());

        return Mono.fromCallable(() -> gson.fromJson(message.body(), CalculateDebtCapacityResponseMessage.class))
                .flatMap(payload ->
                        automaticValidationUseCase.approveRejectAutomaticApplicationStatus(
                                payload.idCreditApplication(),
                                payload.statusName(),
                                payload.paymentPlans())
                )
                .onErrorMap(x -> new SQSProcessorException("Error to listen on SQSProcessorCalculateDebtCapacityResponse"));
    }

}
