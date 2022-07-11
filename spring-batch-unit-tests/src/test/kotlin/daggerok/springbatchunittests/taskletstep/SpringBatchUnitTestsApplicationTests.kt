package daggerok.springbatchunittests.taskletstep

import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.test.JobLauncherTestUtils
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
internal class SpringBatchUnitTestsApplicationTestsApp {

    @Bean
    fun springBatchUnitTestsApplicationTestsAppJob(jobs: JobBuilderFactory, myTaskletStep: Step): Job =
        jobs["springBatchUnitTestsApplicationTestsAppJob"]
            .start(myTaskletStep)
            .build()

    @Bean
    fun springBatchUnitTestsApplicationTestsAppTaskletStep(steps: StepBuilderFactory): TaskletStep =
        steps["springBatchUnitTestsApplicationTestsAppTaskletStep"]
            .tasklet { _, _ ->
                println("tasklet")
                RepeatStatus.FINISHED
            }
            .build()
}

@SpringBootTest
@SpringBatchTest
@TestExecutionListeners(
    JobScopeTestExecutionListener::class,
    StepScopeTestExecutionListener::class,
    DependencyInjectionTestExecutionListener::class,
)
@ContextConfiguration(classes = [SpringBatchUnitTestsApplicationTestsApp::class])
class SpringBatchUnitTestsApplicationTests @Autowired constructor(
    val jobLauncherTestUtils: JobLauncherTestUtils,
) {

    @Test
    fun `should test`() {
        println("run step")
        // jobLauncherTestUtils.launchStep("myTaskletStep")
        // println("run job")
        // jobLauncherTestUtils.launchJob()
    }
}
