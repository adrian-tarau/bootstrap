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
    private RetryProperties properties;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialRandomBackOffPolicy exponentialBackOffPolicy = new ExponentialRandomBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(properties.getInitialInterval());
        exponentialBackOffPolicy.setMaxInterval(properties.getMaxInterval());
        exponentialBackOffPolicy.setMultiplier(properties.getMultiplier());

        List<RetryPolicy> retryPolicies = new ArrayList<>();
        retryPolicies.add(new ExceptionClassifierRetryPolicy());
        retryPolicies.add(new CircuitBreakerRetryPolicy());
        retryPolicies.add(new TimeoutRetryPolicy());
        CompositeRetryPolicy retryPolicy = new CompositeRetryPolicy();
        retryPolicy.setPolicies(retryPolicies.toArray(new RetryPolicy[0]));
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
