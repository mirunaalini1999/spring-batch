package com.example.springbatch.config;

import com.example.springbatch.component.CustomRetryListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate(CustomRetryListener customRetryListener) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // ✅ Exponential Backoff Policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 1s initial wait
        backOffPolicy.setMultiplier(2);  // Each retry doubles the wait time
        backOffPolicy.setMaxInterval(30000);  // Max wait time = 30s
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // ✅ Define retryable exceptions (HTTP 500 & 503)
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(HttpServerErrorException.InternalServerError.class, true); // HTTP 500
        retryableExceptions.put(HttpServerErrorException.ServiceUnavailable.class, true); // HTTP 503

        RetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions); // 3 retries
        retryTemplate.setRetryPolicy(retryPolicy);

        // ✅ Register the Custom Retry Listener
        retryTemplate.registerListener(customRetryListener);

        return retryTemplate;
    }

    // ✅ Define the CustomRetryListener as a Bean
    @Bean
    public CustomRetryListener customRetryListener() {
        return new CustomRetryListener();
    }
}
