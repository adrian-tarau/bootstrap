package net.microfalx.bootstrap.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.List;

@EnableRetry
@Configuration
public class RetryConfig {

    @Autowired
    private RetryProperties properties = new RetryProperties();

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        List<RetryPolicy> retryPolicies = new ArrayList<>();

        ExponentialRandomBackOffPolicy exponentialBackOffPolicy = new ExponentialRandomBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(properties.getInitialInterval());
        exponentialBackOffPolicy.setMaxInterval(properties.getMaxInterval());
        exponentialBackOffPolicy.setMultiplier(properties.getMultiplier());

        ExceptionClassifierRetryPolicy exceptionClassifierRetryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicies.add(exceptionClassifierRetryPolicy);
        CircuitBreakerRetryPolicy circuitBreakerRetryPolicy = new CircuitBreakerRetryPolicy();
        retryPolicies.add(circuitBreakerRetryPolicy);
        TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
        timeoutRetryPolicy.setTimeout(properties.getMaxInterval());
        retryPolicies.add(timeoutRetryPolicy);
        CompositeRetryPolicy retryPolicy = new CompositeRetryPolicy();
        retryPolicy.setPolicies(retryPolicies.toArray(new RetryPolicy[0]));
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
