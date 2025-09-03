package co.com.pragma.creditapplication.consumer.config;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

public interface FilterWebClient {

    ExchangeFilterFunction apply();

}
