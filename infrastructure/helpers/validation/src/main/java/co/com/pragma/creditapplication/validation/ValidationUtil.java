package co.com.pragma.creditapplication.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ValidationUtil {

    private final Validator validator;

    public <T> Mono<T> validate(T body) {
        Set<ConstraintViolation<T>> errors = validator.validate(body);
        if (!errors.isEmpty()) {
            String message = errors.stream()
                    .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                    .reduce((m1, m2) -> m1 + ", " + m2)
                    .orElse("Validation error");
            return Mono.error(new ValidationException(message));
        }
        return Mono.just(body);
    }

}
