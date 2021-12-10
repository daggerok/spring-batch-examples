package dagggerok

import dagggerok.payments.PaymentClient
import dagggerok.payments.PaymentDTO
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.BiFunction
import javax.transaction.Transaction
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.convert.ReadingConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
class SpringBatchConfig {

    @Bean
    fun taskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 1000
            setQueueCapacity(100)
            keepAliveSeconds = 30
        }
}

@Configuration
@EnableBatchProcessing
class MyJobConfig(val jobs: JobBuilderFactory, val steps: StepBuilderFactory, val paymentClient: PaymentClient) {

    @Bean
    fun localStorage(): ConcurrentHashMap<Long, CopyOnWriteArrayList<PaymentDTO>> =
        ConcurrentHashMap<Long, CopyOnWriteArrayList<PaymentDTO>>()

    @Bean
    fun myJob(cleanup: Step, loadAndAggregate: Step, computeAndFilter: Step) =
        jobs["myJob"]
            .start(cleanup)
            .next(loadAndAggregate)
            .next(computeAndFilter)
            .build()

    @Bean
    fun cleanup(localStorage: ConcurrentHashMap<Long, CopyOnWriteArrayList<PaymentDTO>>) =
        steps["cleanup"]
            .tasklet { contribution, chunkContext ->
                logger.info { "clearing $localStorage" }
                localStorage.clear()
                logger.info { "cleared $localStorage" }
                org.springframework.batch.repeat.RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun loadAndAggregate(localStorage: ConcurrentHashMap<Long, CopyOnWriteArrayList<PaymentDTO>>) =
        steps["loadAndAggregate"]
            .tasklet { contribution, chunkContext ->
                logger.info { "enriching $localStorage" }
                paymentClient.getPayments()
                    .forEach {
                        localStorage.merge(
                            it.userId,
                            CopyOnWriteArrayList(listOf(it)),
                            BiFunction { l1, l2 -> CopyOnWriteArrayList(l1 + l2) }
                        )
                    }
                logger.info { "enriched: ${localStorage.values.size}, keys: ${localStorage.keys}" }
                org.springframework.batch.repeat.RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun computeAndFilter(localStorage: ConcurrentHashMap<Long, CopyOnWriteArrayList<PaymentDTO>>) =
        steps["computeAndFilter"]
            .tasklet { contribution, chunkContext ->
                logger.info { "values.sizes: ${localStorage.values.map { it.size }}" }
                logger.info { "cash flows: ${localStorage.values.map { it.sumOf { it.amount } }}" }
                logger.info { "filtering..." }
                val result = localStorage
                    .mapValues { CopyOnWriteArrayList(it.value.filter { it.type != "DEPOSIT" }) }
                    .filterValues { it.sumOf { it.amount } >= BigDecimal(1000) }
                    // .onEach { (k, v) -> logger.info { "k=$k, v=$v" } }
                logger.info { "filtered" }
                logger.info { "withdrawal amounts: ${result.values.map { it.sumOf { it.amount } }}" }
                localStorage.clear()
                localStorage.putAll(result)
                logger.info { "done" }
                localStorage.values.flatten().forEach {
                    logger.info { "User(${it.userId}): ${it.type}(${it.id}) => ${it.amount}" }
                }
                org.springframework.batch.repeat.RepeatStatus.FINISHED
            }
            .build()

    private companion object : KLogging()
}

@RestController
class ReportResource(val jobLauncher: JobLauncher, val myJob: Job) {

    @PostMapping("/api/launch-my-job")
    fun launchMyJob() =
        JobParametersBuilder()
            .addString("at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .toJobParameters()
            .let { jobLauncher.run(myJob, it) }
            .let { mapOf("jobId" to it.jobId) }
}
