package com.gearsofleo.platform.regulatory.ontario.reporting.service.config

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
@EnableConfigurationProperties(JobConfig::class)
class PropsConfig

@ConstructorBinding
@ConfigurationProperties("reporting.app.job")
data class JobConfig(
    val chunkSize: Int,
    val concurrencyLimit: Int,
)

@EnableAsync
@Configuration
class AsyncJobLauncherConfig {

    @Bean
    @Primary
    fun taskExecutor(jobConfig: JobConfig): TaskExecutor =
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
