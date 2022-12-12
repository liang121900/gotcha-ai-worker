package com.gotcha.ai.worker.config.aws

import com.gotcha.ai.worker.config.properties.SqsQueueConfig
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
class SqsClientConfig(val sqsConfig: SqsQueueConfig) : AWSClient() {
	var log: Logger = LoggerFactory.getLogger(SqsClientConfig::class.java)

	@Singleton
	@Named("gotchaAiDetectionRequestSqsAsyncClient")
	fun gotchaAiDetectionRequestSqsAsyncClient(): SqsAsyncClient {

		var clientBuilder = SqsAsyncClient.builder().overrideConfiguration { b ->
			b.apiCallTimeout(Duration.ofMillis(sqsConfig.apiCallTimeout))
			b.apiCallTimeout(Duration.ofMillis(sqsConfig.apiCallTimeout))
		}
		// For connecting to localstack only
		if (serviceEndpoint != null) {
			clientBuilder.endpointOverride(URI(serviceEndpoint))
			clientBuilder.region(Region.US_EAST_1)
		}
		return clientBuilder.build()
	}
}