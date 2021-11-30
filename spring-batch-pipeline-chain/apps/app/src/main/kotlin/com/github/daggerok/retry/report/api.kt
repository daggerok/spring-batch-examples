package com.github.daggerok.pipeline_chain.report

import java.time.LocalDateTime

data class ReportWrapper<T>(
    val jobId: Long,
    val chunks: List<T>,
    val at: LocalDateTime = LocalDateTime.now(),
)
