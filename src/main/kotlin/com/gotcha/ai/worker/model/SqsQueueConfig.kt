package com.gotcha.ai.worker.model

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("gotchaAi.sqs.detectionRequestQueue")
class SqsQueueConfig {

    @NotBlank
    var queueName: String = ""

    var serviceEndpoint: String? = null
}