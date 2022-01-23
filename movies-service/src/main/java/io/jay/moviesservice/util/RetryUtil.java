package io.jay.moviesservice.util;

import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {

    public static Retry retrySpec(Class<?> clazz) {
        var retrySpec = Retry.fixedDelay(3, Duration.ofMillis(500))
                .filter(ex -> clazz.isAssignableFrom(ex.getClass()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    return Exceptions.propagate(retrySignal.failure());
                });

        return retrySpec;
    }
}
