package co.com.pragma.creditapplication.usecase.exception;

public class SelfServiceViolationException extends RuntimeException {
    public SelfServiceViolationException(String message) {
        super(message);
    }
}
