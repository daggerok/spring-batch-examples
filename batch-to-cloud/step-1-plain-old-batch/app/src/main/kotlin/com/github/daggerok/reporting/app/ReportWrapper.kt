package com.github.daggerok.reporting.app

import java.time.Instant

data class ReportWrapper<T>(
    val jobId: Long,
    val chunks: List<T>,
    val at: Instant = Instant.now(),
)
