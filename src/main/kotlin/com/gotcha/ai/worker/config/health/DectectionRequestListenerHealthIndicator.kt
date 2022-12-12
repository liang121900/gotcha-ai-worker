package com.gotcha.ai.worker.config.health

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@Singleton
class DectectionRequestListenerHealthIndicator : HealthIndicator {

    private val log: Logger = LoggerFactory.getLogger(DectectionRequestListenerHealthIndicator::class.java)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DectectionRequestListenerHealthIndicator::class.java)
        private var status: HealthStatus? = HealthStatus.UP
        fun setStatusUp() {
            log.debug("DectectionRequestListener Status is UP")
            status = HealthStatus.UP
        }

        fun setStatusDown() {
            log.error("DectectionRequestListener Status is down")
            status = HealthStatus.DOWN
        }
    }

    override fun getResult(): Publisher<HealthResult> {
        return Mono.just(
            HealthResult.builder("detection-request-sqs-listener")
                .status(status)
                .build()
        )
    }

}