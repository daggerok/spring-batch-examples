package com.github.daggerok.retry.report

import com.github.daggerok.retry.PaymentsReport
import com.github.daggerok.retry.PaymentsReportRepository
import com.github.daggerok.retry.RetryProps
import javax.persistence.EntityManagerFactory
import mu.KLogging
import org.hibernate.SessionFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.database.HibernatePagingItemReader
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.NoBackOffPolicy
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy
import org.springframework.retry.policy.MaxAttemptsRetryPolicy

@Configuration
@EnableBatchProcessing
class PaymentsReportJobConfig(
    private val retryProps: RetryProps,
    private val jobs: JobBuilderFactory,
    private val steps: StepBuilderFactory,
) {

    @Bean
    fun paymentsReportJob(loadAllPayments: Step, enrichUsersData: Step, generateReport: Step): Job =
        jobs["paymentsReport"]
            .start(loadAllPayments)
            .next(enrichUsersData)
            .next(generateReport)
            .build()

    @Bean
    @JobScope
    fun loadAllPayments(loadAllPaymentsTasklet: LoadAllPaymentsTasklet): Step =
        steps["loadAllPayments"]
            .tasklet(loadAllPaymentsTasklet)
            .build()

    @Bean
    @JobScope
    fun enrichUsersData(
        enrichReader: ItemReader<PaymentsReport>,
        enrichProcessor: ItemProcessor<PaymentsReport, PaymentsReport>,
        enrichWriter: ItemWriter<PaymentsReport>,
    ): Step =
        steps["enrichUsersData"]
            .chunk<PaymentsReport, PaymentsReport>(3)
            .reader(enrichReader)
            .processor(enrichProcessor)
            .writer(enrichWriter)
            .faultTolerant()
            .retryLimit(retryProps.maxAttempts)
            .retryPolicy(MaxAttemptsRetryPolicy(retryProps.maxAttempts))
            .build()

    @Bean
    @StepScope
    fun enrichReader(
        @Value("#{stepExecution.jobExecution.jobId}") jobId: Long,
        entityManagerFactory: EntityManagerFactory,
    ): HibernatePagingItemReader<PaymentsReport> =
        HibernatePagingItemReaderBuilder<PaymentsReport>()
            .name("paymentReportEnrichReader")
            .sessionFactory(entityManagerFactory.unwrap(SessionFactory::class.java))
            // .queryString("SELECT pr FROM PaymentsReport pr WHERE pr.jobId = :jobId")
            .queryString("FROM PaymentsReport WHERE jobId = :jobId")
            .parameterValues(mapOf("jobId" to jobId))
            .saveState(false)
            .pageSize(1)
            .useStatelessSession(true)
            .build()

    @Bean
    @StepScope
    fun enrichProcessor(enrichUsersDataProcessor: EnrichUsersDataProcessor) =
        CompositeItemProcessor<PaymentsReport, PaymentsReport>().apply {
            setDelegates(
                listOf(
                    enrichUsersDataProcessor,
                )
            )
        }

    @Bean
    @StepScope
    fun enrichWriter(repository: PaymentsReportRepository) =
        RepositoryItemWriterBuilder<PaymentsReport>()
            .repository(repository)
            .methodName("saveAndFlush")
            .build()

    @Bean
    @JobScope
    fun generateReport(reader: PaymentsReader, writer: PaymentsWriter): Step =
        steps["generateReport"]
            .chunk<ReportWrapper<PaymentsReport>, ReportWrapper<PaymentsReport>>(1)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .retryLimit(retryProps.maxAttempts)
            .retryPolicy(MaxAttemptsRetryPolicy(retryProps.maxAttempts))
            .build()

    private companion object : KLogging()
}
