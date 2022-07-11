package daggerok.springbatchunittests.readprocesswrite

import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.absoluteValue
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.test.JobScopeTestExecutionListener
import org.springframework.batch.test.StepScopeTestExecutionListener
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener

@EnableBatchProcessing
@SpringBootApplication
internal class SpringBatchReaderProcessorWriterTestsApp {

    @Bean
    fun springBatchReaderProcessorWriterTestsAppJob(jobs: JobBuilderFactory, myTaskletStep: Step): Job =
        jobs["springBatchReaderProcessorWriterTestsAppJob"]
            .start(myTaskletStep)
            .build()

    @Bean
    fun springBatchReaderProcessorWriterTestsAppStep(steps: StepBuilderFactory): Step =
        steps["springBatchReaderProcessorWriterTestsAppStep"]
            .chunk<Pair<Instant, Instant>, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>>(2)
            .reader(reader())
            .processor(processor())
            .writer(writer(mapOfLists()))
            .build()

    @Bean
    fun reader(): ListItemReader<Pair<Instant, Instant>> =
        ListItemReader(listOfPairs())

    @Bean
    fun processor(): ItemProcessor<Pair<Instant, Instant>, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> {
        return ItemProcessor<Pair<Instant, Instant>, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> {
            val id = SecureRandom().nextInt(1000).absoluteValue
            println("processing-$id\n\tpair: $it...")
            val from = it.first
            val to = it.second
            ConcurrentHashMap(
                mutableMapOf(
                    "$from" to CopyOnWriteArrayList(
                        listOf(
                            "$from",
                            "$to",
                        )
                    ),
                    "$to" to CopyOnWriteArrayList(
                        listOf(
                            "$from",
                            "$to",
                        )
                    ),
                )
            ).also { println("\t\tprocess-$id-done\t\t\tmap size: ${it.size}") }
        }
    }

    @Bean
    fun writer(mapOfLists: ConcurrentHashMap<String, CopyOnWriteArrayList<String>>): ItemWriter<ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> {
        return ItemWriter<ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> { items ->
            val id = SecureRandom().nextInt(10000).absoluteValue
            println("writing-$id:\n\titems: ${items.size}...")
            items.forEach { item ->
                println("writing-$id:\n\titem: ${item.size}...")
                item.forEach { (k, v) ->
                    println("writing-$id:\n\tk: $k, v: $v...")
                    val prev = mapOfLists[k] ?: CopyOnWriteArrayList()
                    mapOfLists[k] = CopyOnWriteArrayList(prev.plus(v).distinct().sorted())
                    println("\t\twrite-$id-done\n\t\t\tk: $k, mapOfLists[k].size: ${mapOfLists[k]?.size}")
                }
            }
        }
    }

    @Bean
    fun listOfPairs(): CopyOnWriteArrayList<Pair<Instant, Instant>> =
        CopyOnWriteArrayList(
            listOf(
                LocalDateTime.of(2022, 2, 2, 0, 0) to LocalDateTime.of(2022, 2, 2, 1, 59, 59, 999999999),
                LocalDateTime.of(2022, 2, 2, 0, 0) to LocalDateTime.of(2022, 2, 2, 3, 59, 59, 999999999),
                LocalDateTime.of(2022, 2, 2, 0, 0) to LocalDateTime.of(2022, 2, 2, 5, 59, 59, 999999999),
                LocalDateTime.of(2022, 2, 2, 2, 0) to LocalDateTime.of(2022, 2, 2, 3, 59, 59, 999999999),
                LocalDateTime.of(2022, 2, 2, 2, 0) to LocalDateTime.of(2022, 2, 2, 5, 59, 59, 999999999),
            ).map {
                it.first.toInstant(ZoneOffset.UTC) to it.second.toInstant(ZoneOffset.UTC)
            }
        )

    @Bean
    fun mapOfLists(): ConcurrentHashMap<String, CopyOnWriteArrayList<String>> =
        ConcurrentHashMap()
}

@SpringBootTest
@SpringBatchTest
@TestExecutionListeners(
    JobScopeTestExecutionListener::class,
    StepScopeTestExecutionListener::class,
    DependencyInjectionTestExecutionListener::class,
)
@ContextConfiguration(classes = [SpringBatchReaderProcessorWriterTestsApp::class])
class SpringBatchReaderProcessorWriterTests(@Autowired val mapOfLists: ConcurrentHashMap<String, CopyOnWriteArrayList<String>>) {

    @Test
    fun `should test reader processor writer`() {
        println("run reader processor writer step")
        println("mapOfLists:")
        mapOfLists.forEach { (k, v) ->
            println("  k: $k,")
            println("  v: ")
            v.forEach { println("    - $it ") }
        }
    }
}
