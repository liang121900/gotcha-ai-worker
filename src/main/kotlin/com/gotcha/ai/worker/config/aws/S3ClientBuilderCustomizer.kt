package com.gotcha.ai.worker.config.aws

import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton
import software.amazon.awssdk.services.s3.S3ClientBuilder
import java.net.URI

@Requires(env = ["local"])
@Singleton
class S3ClientBuilderCustomizer : AWSClient(), BeanCreatedEventListener<S3ClientBuilder> {
	override fun onCreated(@NonNull event: BeanCreatedEvent<S3ClientBuilder>): S3ClientBuilder? {
		var builder = event.bean
		builder = if (serviceEndpoint != null) event.bean.endpointOverride(URI(serviceEndpoint)) else builder
		return builder
	}
}