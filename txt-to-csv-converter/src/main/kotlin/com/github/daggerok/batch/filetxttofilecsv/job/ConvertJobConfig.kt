package com.github.daggerok.batch.filetxttofilecsv.job

import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource

@Configuration
class ConvertJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    fun convertJob(): Job =
        jobBuilderFactory.get("convertJob")
            .start(convertTxtFileToCvsFileStep())
            .build()

    @Bean
    fun convertTxtFileToCvsFileStep(): Step =
        stepBuilderFactory.get("convertTxtFileToCvsFileStep")
            .chunk<String, String>(1)
            .reader(txtFileReader())
            .processor(txtToCsvConverterProcessor())
            .writer(cvsLoggerWriter())
            .build()

    @Bean
    fun txtFileReader() =
        FlatFileItemReader<String?>().apply {
            setName("txtFileReader")
            setResource(FileSystemResource("target/input.txt"))
            val lineMapper = DefaultLineMapper<String>()
            lineMapper.setFieldSetMapper { it.values.joinToString(" ") }
            lineMapper.setLineTokenizer(DelimitedLineTokenizer(" "))
            setLineMapper(lineMapper)
        }

    @Bean
    fun txtToCsvConverterProcessor() =
        ItemProcessor<String, String> {
            val split = it.split(" ")
            val result = split.joinToString(",")
            logger.info { "converting: $it -> $result" }
            result
        }

    @Bean
    fun cvsLoggerWriter() =
        ItemWriter<String> {
            logger.info { "writing item: $it" }
        }

    private companion object : KLogging()
}
