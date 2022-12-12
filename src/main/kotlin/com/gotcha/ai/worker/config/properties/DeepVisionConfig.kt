package com.gotcha.ai.worker.config.properties

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("gotchaAi.deepVision")
class DeepVisionConfig {

	@NotBlank
	var basePath: String = ""
}
