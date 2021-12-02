package com.github.daggerok.retry.report

import java.time.LocalDateTime

data class ReportWrapper<T>(
    val jobId: Long,
    val chunks: List<T>,
    val at: LocalDateTime = LocalDateTime.now(),
)

data class LaunchDocument(
    val jobId: Long,
)

data class ErrorDocument(
    val message: String? = "Unknown error",
)
