package com.gotcha.ai.worker.health

import com.gotcha.ai.worker.service.SqsService
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@Singleton
class SqsHealthIndicator(@Inject val sqsService: SqsService) : HealthIndicator {

    private val log: Logger = LoggerFactory.getLogger(SqsHealthIndicator::class.java)

    override fun getResult(): Publisher<HealthResult> {
        return sqsService.getQueueAttributes().map { _ ->
            HealthResult.builder("sqs-health")
                .status(HealthStatus.UP)
                .build()
        }.onErrorResume {  e ->
            Mono.just(
                HealthResult.builder("sqs-health")
                    .status(HealthStatus.DOWN)
                    .exception(e)
                    .build()
            )
        }
    }
}