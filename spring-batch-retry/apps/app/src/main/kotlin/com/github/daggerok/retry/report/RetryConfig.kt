package com.github.daggerok.retry.report

import com.github.daggerok.retry.PropsConfig
import com.github.daggerok.retry.RetryProps
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

@EnableRetry
@Configuration
@Import(PropsConfig::class)
class RetryConfig {

    @Bean
    fun retryTemplate(retryProps: RetryProps): RetryTemplate =
        RetryTemplate().apply {
            setBackOffPolicy(
                ExponentialBackOffPolicy().apply {
                    initialInterval = retryProps.initialDelay
                }
            )
            setRetryPolicy(
                SimpleRetryPolicy(
                    retryProps.maxAttempts
                )
            )
        }
}
