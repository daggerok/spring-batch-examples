package com.daggerok.spring.batch.cfg;

import com.daggerok.spring.batch.SpringBootBatchApplication;
import com.daggerok.spring.batch.model.Data;
import com.daggerok.spring.batch.model.DataProcessor;
import com.daggerok.spring.batch.model.DataReader;
import com.daggerok.spring.batch.model.DataWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Configuration
@EnableScheduling
@EnableBatchProcessing
@ComponentScan(basePackageClasses = SpringBootBatchApplication.class)
public class Cfg {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("com.daggerok.spring.batch.cfg.Cfg.job")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .start(taskletStep())
                .next(processorStep())
                .build();
    }

    @Bean
    public Step taskletStep() {
        return stepBuilderFactory.get("com.daggerok.spring.batch.cfg.Cfg.taskletStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("\ncontrib: {}\n, ctx: {}\nrun tasklet first", contribution, chunkContext);
                    return null;
                })
                .build();
    }

    @Bean
    public Step processorStep() {
        return stepBuilderFactory.get("com.daggerok.spring.batch.cfg.Cfg.processorStep")
                .<Data, Data>chunk(2)
                .reader(dataReader())
                .processor(dataProcessor())
                .writer(dataWriter())
                .build();
    }

    @Bean
    public JobExecutionListenerSupport listener() {
        return new JobExecutionListenerSupport() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("\nlistener says before job");
            }
        };
    }

    @Bean
    public DataReader dataReader() {
        return new DataReader();
    }

    @Bean
    DataProcessor dataProcessor() {
        return new DataProcessor();
    }

    @Bean
    public DataWriter dataWriter() {
        return new DataWriter();
    }
}
