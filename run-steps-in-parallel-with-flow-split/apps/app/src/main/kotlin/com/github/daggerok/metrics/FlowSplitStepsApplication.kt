package com.github.daggerok.metrics

import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlowSplitStepsApplication

fun main(args: Array<String>) {
    runApplication<FlowSplitStepsApplication>(*args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.from(ZoneOffset.UTC)))
    }
}
