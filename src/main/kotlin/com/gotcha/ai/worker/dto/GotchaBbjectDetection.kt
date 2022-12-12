package com.gotcha.ai.worker.dto

import io.micronaut.core.annotation.NonNull
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.validation.constraints.NotBlank

/**
 *
 * This class represents the detection request object on dynamoDB
 * Please check the example on doc/schema for details
 *
 * Note: have to set default value because needs a default primary constructor by DynamoDB client
 */
@DynamoDbBean
data class GotchaObjectDetection(
		@NotBlank
		@get:DynamoDbPartitionKey
		@get:DynamoDbSecondarySortKey(indexNames = ["gsi-sk-pk"])
		var pk: String = "",

		@NonNull
		@get:DynamoDbSortKey
		@get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi-sk-pk"])
		var sk: String = "",

		var data_1: String = "",

		@get:DynamoDbSecondaryPartitionKey(indexNames = ["lsk-created-date"])
		var created_date: String = "",

		@get:DynamoDbSecondaryPartitionKey(indexNames = ["lsk-updated-date"])
		var last_updated_date: String = "",
)
