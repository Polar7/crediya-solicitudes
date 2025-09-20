package co.com.pragma.creditapplication.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     String queueEmailUpdateStatusCreditApplicationUrl,
     String queueInitFlowAutomaticValidationUrl,
     String queueCalculateDebtCapacityUrl,
     String endpoint){
}
