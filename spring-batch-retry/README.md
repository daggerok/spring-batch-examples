# Spring Batch Retry
This repository contains Retry mechanism implemented in Spring-Batch project

## Overview
Use regular RetryTemplate from spring-retry

### Configure retry

```properties
reporting.app.retry.maxAttempts=33
reporting.app.retry.initialDelay=333
```

```kotlin
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

@ConstructorBinding
@ConfigurationProperties("reporting.app.retry")
data class RetryProps(
    val maxAttempts: Int,
    val initialDelay: Long,
)
```

### Use retry

#### RetryTemplate

```kotlin
@JobScope
@Component
class MyTasklet(
    private val retryTemplate: RetryTemplate, // (*)
    private val myService: MyServiceUsesSpringDataJpa,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? = run {
        val jobId = chunkContext.stepContext.jobInstanceId
        println("Try executing MyTasklet(jobId=$jobId)")
        val result = retryTemplate.execute<MyData, Throwable> { // (*)
            println("A try:")
            myService.writableMethod(jobId, myData)
        }
        println("A try result: $result")
        RepeatStatus.FINISHED
    }
}
```

#### @Retryable annotation

```kotlin
@StepScope
@Component
class EnrichUsersDataProcessor(private val myClient: MyClient) : ItemProcessor<MyData, MyData> {

    @Retryable // (*)
    override fun process(item: MyData): MyData =
        myClient.getData(item.id).run {
            item.copy(
                fullName = "$firstName $lastName",
            )
        }
}
```

But:
If any method in retry is market with @Transactional
annotation, then make sure you have done it like so:

```kotlin
@Service
@Transactional(readOnly = true, propagation = REQUIRES_NEW)
class MyServiceUsesSpringDataJpa(val myRepository: MySpringDataJpaRepository) {

    @Transactional(readOnly = false, propagation = REQUIRES_NEW)
    fun writableMethod(/*...*/): MyData =
        myRepository.save(/*...*/) // saveAll(/*...*/) // deleteAll() // etc...

    fun readableMethod(/*...*/): List<MyData> =
        myRepository.findAll() // findById(/*...*/) // count() // etc...
}
```

Transaction propagation must be `REQUIRED_NEW`

## Build, run, test

```bash
rm -rf ~/.m2/repository/com/github/daggerok
mvn clean package install

mvn spring-boot:start -f apps/user-service
mvn spring-boot:start -f apps/payment-service
mvn spring-boot:start -f apps/app

http post :8080/api/launch-payments-report ; sleep 20s ; http get :8080/api

mvn spring-boot:stop -f apps/app
mvn spring-boot:stop -f apps/user-service
mvn spring-boot:stop -f apps/payment-service

cat ./apps/app/target/payments-report.csv
```

<!--

```bash
cd spring-batch-retry ; ./mvnw -f docker -P down ; ./mvnw -f docker -P up
rm -rf ~/.m2/repository/com/github/daggerok/batch ; ./mvnw clean install
./mvnw -f apps/app spring-boot:run
./mvnw -f docker -P down
```

```bash
cd spring-batch-retry ; ./mvnw clean test
```

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.5/maven-plugin/reference/html/#build-image)
* [Spring Batch](https://docs.spring.io/spring-boot/docs/2.5.5/reference/htmlsingle/#howto-batch-applications)

### Guides

The following guides illustrate how to use some features concretely:

* [Creating a Batch Service](https://spring.io/guides/gs/batch-processing/)

-->
