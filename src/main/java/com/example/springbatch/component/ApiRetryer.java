package com.example.springbatch.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ApiRetryer {

    private final RetryTemplate retryTemplate;
    private final RestTemplate restTemplate;

    public ApiRetryer(RetryTemplate retryTemplate, RestTemplate restTemplate) {
        this.retryTemplate = retryTemplate;
        this.restTemplate = restTemplate;
    }

    public String triggerApiWithRetry(String url) throws Exception {

        return retryTemplate.execute(context -> {

            try {
                return restTemplate.getForObject(url, String.class);
            } catch (HttpClientErrorException | HttpServerErrorException e) {

                log.error("HTTP server or client exception occurred. ", e);
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {

                    log.error("SERVICE_UNAVAILABLE or INTERNAL_SERVER_ERROR exception occurred. ");
                    throw e;
                }
                throw new Exception("API call failed with error: " + e.getMessage());
            }
        });
    }
}

