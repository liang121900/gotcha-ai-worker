package com.gotcha.ai.worker.config.aws

import io.micronaut.context.annotation.Value

abstract class AWSClient {

	@Value("\${gotcha-ai.aws.service-endpoint}")
	var serviceEndpoint: String? = null

}