package co.com.pragma.creditapplication.sqs.sender;

import co.com.pragma.creditapplication.model.creditapplication.gateways.ProducerMessagingBroker;
import co.com.pragma.creditapplication.sqs.sender.config.SQSSenderProperties;
import co.com.pragma.creditapplication.sqs.sender.dto.SendEmailUpdateCreditApplicationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements ProducerMessagingBroker {

    private final SQSSenderProperties properties;

    private final SqsAsyncClient client;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendMessageUpdateCreditApplication(Long idApplication, String emailClient, String statusName) {
        try {
            return send(objectMapper.writeValueAsString(new SendEmailUpdateCreditApplicationMessage(idApplication, emailClient, statusName)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing message", e);
        }
    }

    private Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

}
