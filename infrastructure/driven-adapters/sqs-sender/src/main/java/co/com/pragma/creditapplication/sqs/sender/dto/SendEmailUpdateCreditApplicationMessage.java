package co.com.pragma.creditapplication.sqs.sender.dto;

public record SendEmailUpdateCreditApplicationMessage(Long idApplication,
                                                      String emailClient,
                                                      String statusName) {
}
