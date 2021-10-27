package dagggerok.springbatchjobscheduling

import java.util.UUID
import java.util.concurrent.Executors
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.repeat.RepeatStatus.FINISHED
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@EnableAsync
@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication(exclude = [BatchAutoConfiguration::class])
class SpringBatchJobSchedulingApplication(val jobLauncher: JobLauncher, val generateReportJob: Job) {

    @Bean
    fun generateReport(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 1000
            setQueueCapacity(100)
            keepAliveSeconds = 30
        }

    @Async
    @Scheduled(cron = "\${spring-batch-job-scheduling.each-3-seconds-cron}")
    fun runJob() =
        JobParametersBuilder()
            .addString("JobUUID", System.currentTimeMillis().toString() + "-" + UUID.randomUUID().toString())
            .toJobParameters()
            .let { jobLauncher.run(generateReportJob, it) }
            .let { logger.info { "job ${it.jobId} done." } }

    companion object : KLogging()
}

fun main(args: Array<String>) {
    runApplication<SpringBatchJobSchedulingApplication>(*args)
}

@Configuration
class JobConfig() {

    @Bean
    fun repository(): MutableMap<String, String> =
        mutableMapOf()

    @Bean
    fun reportRepository(): MutableMap<String, String> =
        mutableMapOf()

    @Bean
    fun initApp(repository: MutableMap<String, String>): InitializingBean = InitializingBean {
        repository["users"] = listOf("user1", "user2").joinToString(";")
        repository["payments"] = listOf("user1payment", "user2payment1", "user2payment2").joinToString(";")
    }

    @Bean
    fun fetchUsersStep(
        steps: StepBuilderFactory,
        repository: MutableMap<String, String>,
        reportRepository: MutableMap<String, String>
    ): Step =
        steps.get("fetchUsersStep")
            .tasklet { contribution, chunkContext ->
                val anys = repository["users"]?.split(";") ?: arrayListOf()
                val users = anys.joinToString(",")
                reportRepository["users"] = users
                FINISHED
            }
            .build()

    @Bean
    fun fetchPaymentsStep(
        steps: StepBuilderFactory,
        repository: MutableMap<String, String>,
        reportRepository: MutableMap<String, String>
    ): Step =
        steps.get("fetchPaymentsStep")
            .tasklet { contribution, chunkContext ->
                val anys = repository["payments"]?.split(";") ?: arrayListOf()
                val payments = anys.joinToString(",")
                reportRepository["payments"] = payments
                FINISHED
            }
            .build()

    @Bean
    fun aggregationStep(steps: StepBuilderFactory, reportRepository: MutableMap<String, String>): Step =
        steps.get("aggregationStep")
            .tasklet { contribution, chunkContext ->
                val users = reportRepository["users"]?.split(",") ?: arrayListOf()
                val payments = reportRepository["payments"]?.split(",") ?: arrayListOf()
                for (user in users) reportRepository[user] =
                    payments.filter { it.startsWith(user) }.joinToString(", ")
                FINISHED
            }
            .build()

    @Bean
    fun printReportStep(steps: StepBuilderFactory, reportRepository: MutableMap<String, String>): Step =
        steps.get("printReportStep")
            .tasklet { contribution, chunkContext ->
                reportRepository.remove("users")
                reportRepository.remove("payments")
                reportRepository.forEach { (user, payments) -> logger.info { "$user: $payments" } }
                FINISHED
            }
            .build()

    @Bean
    fun generateReportJob(
        jobs: JobBuilderFactory,
        fetchUsersStep: Step,
        fetchPaymentsStep: Step,
        aggregationStep: Step,
        printReportStep: Step
    ): Job =
        jobs.get("generateReportJob")
            .start(fetchUsersStep)
            .next(fetchPaymentsStep)
            .next(aggregationStep)
            .next(printReportStep)
            .build()

    companion object : KLogging()
}
