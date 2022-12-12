package com.gotcha.ai.worker.config.properties

import io.micronaut.context.annotation.Requires
import javax.validation.constraints.NotBlank
import io.micronaut.context.annotation.ConfigurationProperties

@Requires(property = "gotcha-ai.dynamodb.table-name")
@ConfigurationProperties("gotchaAi.dynamodb")
class DynamoConfig {
	@NotBlank
	var tableName: String = ""
	var autoCreate: Boolean = false
}


