package com.github.daggerok.reporting.app

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlin.reflect.jvm.jvmName

inline fun <reified R : Any?, C : Any> MeterRegistry.measure(component: C, jobId: Long? = null, name: String? = null, execution: () -> R): R =
    Timer.start(this).let { timer ->
        val execution = runCatching { execution() }
        timer.stop(
            Timer.builder(name ?: component::class.jvmName)
                .tag("component", component::class.jvmName)
                .tag("status", if (execution.isSuccess) "success" else "failure")
                .apply { if (jobId != null) tag("jobId", "%d".format(jobId)) }
                .apply { if (name != null) tag("name", "%s".format(name)) }
                .register(this)
        )
        execution.getOrThrow()
    }
