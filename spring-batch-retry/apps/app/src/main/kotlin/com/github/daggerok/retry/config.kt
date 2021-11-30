package com.github.daggerok.retry

import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableConfigurationProperties(AppProps::class, JobProps::class, RetryProps::class)
class PropsConfig

@ConstructorBinding
@ConfigurationProperties("reporting.app")
data class AppProps(
    val job: JobProps,
    val retry: RetryProps,
)

@ConstructorBinding
@ConfigurationProperties("reporting.app.job")
data class JobProps(
    val chunkSize: Int,
    val concurrencyLimit: Int,
)

@ConstructorBinding
@ConfigurationProperties("reporting.app.retry")
data class RetryProps(
    val maxAttempts: Int,
    val initialDelay: Long,
)

@EnableAsync
@Configuration
class AsyncJobLauncherConfig {

    @Bean
    @Primary
    fun taskExecutor(jobConfig: JobProps): TaskExecutor =
        SimpleAsyncTaskExecutor("async-exec-")
            .apply { concurrencyLimit = jobConfig.concurrencyLimit }

    @Bean
    fun asyncLauncher(jobRepository: JobRepository, taskExecutor: TaskExecutor): JobLauncher =
        SimpleJobLauncher().apply {
            setJobRepository(jobRepository)
            setTaskExecutor(taskExecutor)
            afterPropertiesSet()
        }
}
