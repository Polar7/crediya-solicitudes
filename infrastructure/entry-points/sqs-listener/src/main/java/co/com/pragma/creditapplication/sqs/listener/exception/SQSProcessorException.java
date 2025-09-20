package co.com.pragma.creditapplication.sqs.listener.exception;

public class SQSProcessorException extends RuntimeException {
    public SQSProcessorException(String message) {
        super(message);
    }
}
