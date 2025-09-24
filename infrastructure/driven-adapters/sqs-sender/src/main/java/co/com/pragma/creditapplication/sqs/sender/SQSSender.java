package co.com.pragma.creditapplication.sqs.sender;

import co.com.pragma.creditapplication.model.creditapplication.NewApplicationInformation;
import co.com.pragma.creditapplication.model.creditapplication.PaymentPlan;
import co.com.pragma.creditapplication.model.creditapplication.SelectCreditsApproved;
import co.com.pragma.creditapplication.model.creditapplication.gateways.ProducerMessagingBroker;
import co.com.pragma.creditapplication.sqs.sender.config.SQSSenderProperties;
import co.com.pragma.creditapplication.sqs.sender.dto.CalculateDebtCapacityMessage;
import co.com.pragma.creditapplication.sqs.sender.dto.EmailUpdateCreditApplicationMessage;
import co.com.pragma.creditapplication.model.creditapplication.InitFlowAutomaticValidationEmailMessage;
import co.com.pragma.creditapplication.sqs.sender.dto.MetricCreditApproved;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SQSSender implements ProducerMessagingBroker {

    private final SQSSenderProperties properties;

    private final SqsAsyncClient client;

    private final Gson gson;

    @Override
    public Mono<Void> sendUpdateCreditApplication(Long idApplication, String emailClient, String statusName, List<PaymentPlan> paymentPlans) {
        log.info("SQSSender sendUpdateCreditApplication");
        var jsonMessage = gson.toJson(new EmailUpdateCreditApplicationMessage(idApplication, emailClient, statusName, paymentPlans));
        return send(jsonMessage, properties.queueEmailUpdateStatusCreditApplicationUrl()).then();
    }

    @Override
    public Mono<Void> sendInitFlowAutomaticValidation(String emailClient, BigDecimal salaryClient, NewApplicationInformation newApplicationInformation) {
        log.info("SQSSender sendInitFlowAutomaticValidation");
        var jsonMessage = gson.toJson(new InitFlowAutomaticValidationEmailMessage(emailClient, salaryClient, newApplicationInformation));
        return send(jsonMessage, properties.queueInitFlowAutomaticValidationUrl()).then();
    }

    @Override
    public Mono<Void> sendCalculateDebtCapacity(List<SelectCreditsApproved> listCreditsApproved, BigDecimal salaryClient, NewApplicationInformation newApplicationInformation) {
        log.info("SQSSender sendCalculateDebtCapacity");
        var jsonMessage = gson.toJson(new CalculateDebtCapacityMessage(listCreditsApproved, salaryClient, newApplicationInformation));
        return send(jsonMessage, properties.queueCalculateDebtCapacityUrl()).then();
    }

    @Override
    public Mono<Void> sendMetricCreditApproved(BigDecimal amountApproved) {
        log.info("SQSSender sendMetricReportApproved");
        var jsonMessage = gson.toJson(new MetricCreditApproved(amountApproved));
        return send(jsonMessage, properties.queueMetricCreditApprovedUrl()).then();
    }

    private Mono<String> send(String message, String queueUrl) {
        log.info("Message to send {}", message);
        return Mono.fromCallable(() -> buildRequest(message, queueUrl))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .doOnError(error -> log.error("Error sending message {}", error.getMessage()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
    }

}
