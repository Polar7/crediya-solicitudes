package co.com.pragma.creditapplication.api.exception;

import co.com.pragma.creditapplication.api.dto.GenericResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = ex.getMessage();

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GenericResponseDto<String> errorDto = GenericResponseDto.of(status.value(), "ERROR", detail);

        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer;
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(errorDto);
            buffer = bufferFactory.wrap(jsonBytes);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        log.error("❌ Error ❌");
        log.error(ex.getMessage(), ex);
        return response.writeWith(Mono.just(buffer));
    }

}
