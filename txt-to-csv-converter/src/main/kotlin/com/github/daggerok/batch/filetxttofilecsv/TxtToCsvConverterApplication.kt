package com.github.daggerok.batch.filetxttofilecsv

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableBatchProcessing
@SpringBootApplication
class FileTxtToFileCsvApplication

fun main(args: Array<String>) {
    runApplication<FileTxtToFileCsvApplication>(*args)
}
