package com.github.daggerok.metrics.app

import com.github.daggerok.payment.api.PaymentDTO
import com.github.daggerok.user.api.UserDTO
import io.micrometer.core.instrument.MeterRegistry
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.support.SimpleFlow
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate

@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
class PaymentsReportJobConfig(
    private val meter: MeterRegistry,
    private val jobs: JobBuilderFactory,
    private val steps: StepBuilderFactory,
    private val transactionTemplate: TransactionTemplate,
    private val paymentsReportService: PaymentsReportService,
) {

    @Bean
    fun paymentsReportJob(
        loadAllPaymentsAndUsersFlow: SimpleFlow,
        cleanupUsersAndPaymentsTaskletStep: TaskletStep,
        enrichReportTaskletStep: TaskletStep,
        generateReport: Step,
    ): Job =
        jobs["paymentsReport"]
            .start(loadAllPaymentsAndUsersFlow)
            .next(cleanupUsersAndPaymentsTaskletStep)
            .next(enrichReportTaskletStep)
            .next(generateReport)
            .build() // <- build: flow
            .build() // <- build: job

    @Bean
    fun loadAllPaymentsAndUsersFlow(
        loadAllPaymentsAndUsersTaskExecutor: TaskExecutor,
        simpleAsyncTaskExecutor: TaskExecutor,
        loadAllPaymentsFlow: Flow,
        loadAllUsersFlow: Flow,
    ): SimpleFlow =
        FlowBuilder<SimpleFlow>("loadAllPaymentsAndUsersFlow")
            .split(simpleAsyncTaskExecutor)
            .add(loadAllUsersFlow, loadAllPaymentsFlow)
            .build()

    @Bean
    fun loadAllUsersFlow(usersFlowData: MutableMap<Long, UserDTO>): Flow =
        FlowBuilder<SimpleFlow>("loadAllUsersFlow")
            .from(
                steps["loadAllUsersStep"]
                    .tasklet { _, _ ->
                        meter.measure(this, name = "app.loadAllUsersFlow") {
                            logger.info { "loadAllUsersFlow -> loadAllUsersStep..." }
                            paymentsReportService.loadAllUsers()
                                .forEach { usersFlowData[it.id] = it }
                            logger.info { "loadAllUsersFlow -> loadAllUsersStep done." }
                            RepeatStatus.FINISHED
                        }
                    }
                    .build()
            )
            .build()

    @Bean
    fun loadAllPaymentsFlow(paymentsFlowData: MutableMap<Long, List<PaymentDTO>>): Flow =
        FlowBuilder<SimpleFlow>("loadAllPaymentsFlow")
            .from(
                steps["loadAllPaymentsStep"]
                    .tasklet { _, _ ->
                        meter.measure(this, name = "app.loadAllPaymentsFlow") {
                            logger.info { "loadAllPaymentsFlow -> loadAllPaymentsStep..." }
                            paymentsFlowData.putAll(
                                paymentsReportService.loadAllPayments()
                                    .groupBy { it.id }
                            )
                            logger.info { "loadAllPaymentsFlow -> loadAllPaymentsStep done." }
                            RepeatStatus.FINISHED
                        }
                    }
                    .build()
            )
            .build()

    @Bean
    fun cleanupUsersAndPaymentsTaskletStep(): TaskletStep =
        steps["cleanupUsersAndPaymentsTaskletStep"]
            .tasklet { _, chunkContext ->
                meter.measure(this, name = "app.cleanupUsersAndPaymentsTaskletStep", jobId = chunkContext.stepContext.jobInstanceId) {
                    logger.info { "cleanupUsersAndPaymentsTaskletStep..." }
                    val jobId = chunkContext.stepContext.jobInstanceId
                    transactionTemplate.execute {
                        paymentsReportService.dropReportRowsIfAny(jobId)
                    }
                    logger.info { "cleanupUsersAndPaymentsTaskletStep done." }
                    RepeatStatus.FINISHED
                }
            }
            .build()

    @Bean
    @JobScope
    fun enrichReportTaskletStep(
        @Value("#{jobExecution.jobId}") jobId: Long,
        // @Value("#{stepExecution.jobExecution.jobId}") jobId: Long,
        paymentsFlowData: MutableMap<Long, List<PaymentDTO>>,
        usersFlowData: MutableMap<Long, UserDTO>,
    ): TaskletStep =
        steps["enrichReportTaskletStep"]
            .tasklet { _, _ ->
                meter.measure(this, name = "app.enrichReportTaskletStep") {
                    logger.info { "enrichReportTaskletStep..." }
                    paymentsFlowData.values.flatten()
                        .map {
                            val userDTO = usersFlowData[it.userId] ?: throw RuntimeException("User(id=${it.userId}) not found")
                            PaymentsReport(
                                jobId = jobId,
                                //
                                paymentId = it.id,
                                paymentType = it.type,
                                paymentAmount = it.amount,
                                paymentDateTime = it.createdAt,
                                //
                                userId = userDTO.id,
                                userFullName = userDTO.run { "$firstName $lastName" },
                                userRegistrationDate = userDTO.createdAt?.atOffset(ZoneOffset.UTC)?.toLocalDate(),
                            )
                        }
                        .forEach { row ->
                            transactionTemplate.execute {
                                paymentsReportService.addItem(row)
                            }
                        }
                    logger.info { "enrichReportTaskletStep done." }
                    RepeatStatus.FINISHED
                }
            }
            .build()

    @Bean
    fun generateReport(reader: PaymentsReader, writer: PaymentsWriter): Step =
        steps["generateReport"]
            .chunk<ReportWrapper<PaymentsReport>, ReportWrapper<PaymentsReport>>(1)
            .reader(reader)
            .writer(writer)
            .build()

    // private

    @Bean
    fun usersFlowData(): MutableMap<Long, UserDTO> =
        ConcurrentHashMap()

    @Bean
    fun paymentsFlowData(): MutableMap<Long, List<PaymentDTO>> =
        ConcurrentHashMap()

    private companion object : KLogging()
}
