package com.gotcha.ai.worker.config.properties

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("gotchaAi.sqs.detectionRequestQueue")
class SqsQueueConfig {

	@NotBlank
	var queueName: String = ""

	@NotBlank
	var queueUrl: String = ""

	var apiCallTimeout: Long = 120000

	var apiCallAttemptTimeout: Long = 30000

}