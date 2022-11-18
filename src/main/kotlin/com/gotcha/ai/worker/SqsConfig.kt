package com.gotcha.ai.worker

import com.gotcha.ai.worker.model.SqsQueueConfig
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI
import java.time.Duration


@Factory
class SqsConfig(val sqsConfig: SqsQueueConfig) {
    var log: Logger = LoggerFactory.getLogger(SqsConfig::class.java)
    /*val CONNECTION_FACTORY_BEAN_NAME = "sqsJmsConnectionFactory"*/


    @Singleton
    @Named("gotchaAiDetectionRequestSqsAsyncClient")
    fun gotchaAiDetectionRequestSqsAsyncClient(): SqsAsyncClient {
        var clientBuilder = SqsAsyncClient.builder()
            .overrideConfiguration { b ->
                b.apiCallTimeout(Duration.ofMillis(sqsConfig.apiCallTimeout))
                b.apiCallTimeout(Duration.ofMillis(sqsConfig.apiCallTimeout))
            }
        // For local only
        if (sqsConfig.serviceEndpoint != null) {
            clientBuilder.endpointOverride(URI(sqsConfig.serviceEndpoint))
            clientBuilder.region(Region.US_EAST_1)
        }
        return clientBuilder.build()
    }
}