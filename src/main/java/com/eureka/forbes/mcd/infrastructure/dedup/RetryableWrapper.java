package com.eureka.forbes.mcd.infrastructure.dedup;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class RetryableWrapper {

    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void withRetry(Runnable runnable) {
        runnable.run();
    }
}
