package com.example.springbatch.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryCallback;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

        log.info("Retry attempt " + context.getRetryCount() + " failed: " + throwable.getMessage());

        //TODO need to update failed in sub job
        //TODO need to change the status as COMPLETED TO FAILED OR SUCCESS in both parent and sub job
    }
}
