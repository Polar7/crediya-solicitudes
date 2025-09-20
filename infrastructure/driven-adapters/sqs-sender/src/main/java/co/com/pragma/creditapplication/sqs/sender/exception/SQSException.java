package co.com.pragma.creditapplication.sqs.sender.exception;

public class SQSException extends RuntimeException {
    public SQSException(String message) {
        super(message);
    }
}
