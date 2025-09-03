package co.com.pragma.creditapplication.api;

import co.com.pragma.creditapplication.api.dto.CreateCreditApplicationDTO;
import co.com.pragma.creditapplication.api.dto.GenericResponseDto;
import co.com.pragma.creditapplication.api.mapper.CreditApplicationMapper;
import co.com.pragma.creditapplication.usecase.credit.CreditUseCase;
import co.com.pragma.creditapplication.validation.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    private static final String MESSAGE_CREATED_CREDIT = "Credit application created successful";

    private final CreditUseCase creditUseCase;

    private final ValidationUtil validationUtil;

    private final CreditApplicationMapper creditApplicationMapper;

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        log.info("Received POST request create credit application");

        return serverRequest.principal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(principal -> {
                    String userId = principal.getTokenAttributes().get("sub").toString();
                    return serverRequest.bodyToMono(CreateCreditApplicationDTO.class)
                            .flatMap(validationUtil::validate)
                            .map(creditApplicationMapper::toModel)
                            .flatMap(creditApplication -> {
                                creditApplication.setIdClient(Long.valueOf(userId));
                                return creditUseCase.createCreditApplication(creditApplication);
                            });
                })
                .doOnSuccess(aVoid -> log.info(MESSAGE_CREATED_CREDIT))
                .thenReturn(GenericResponseDto.of(HttpStatus.OK.value(), "OK", MESSAGE_CREATED_CREDIT))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
    }

}
