package com.github.daggerok.metrics.app

import java.time.Instant

data class ReportWrapper<T>(
    val jobId: Long,
    val chunks: List<T>,
    val at: Instant = Instant.now(),
)
