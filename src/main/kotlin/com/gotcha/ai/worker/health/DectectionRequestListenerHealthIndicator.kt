package com.gotcha.ai.worker.health

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

@Singleton
class DectectionRequestListenerHealthIndicator : HealthIndicator {

    companion object {
        private var status: HealthStatus? = HealthStatus.UP
        fun setStatusUp() {
            status = HealthStatus.UP
        }

        fun setStatusDown() {
            status = HealthStatus.DOWN
        }
    }

    override fun getResult(): Publisher<HealthResult> {
        return Mono.just(
            HealthResult.builder("detection-request-queue-listener")
                .status(status)
                .build()
        )
    }

}