package com.gotcha.ai.worker.dao;

import com.gotcha.ai.worker.CIAwsCredentialsProviderChainCondition
import com.gotcha.ai.worker.CIAwsRegionProviderChainCondition
import com.gotcha.ai.worker.config.properties.DynamoConfig
import com.gotcha.ai.worker.dto.GotchaObjectDetection
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.*
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.util.*


@Requirements(
		Requires(beans = [DynamoDbClient::class, DynamoConfig::class]),
		Requires(condition = CIAwsCredentialsProviderChainCondition::class),
		Requires(condition = CIAwsRegionProviderChainCondition::class),
)
@Singleton
class DetectionRequestDaoDynamo(private val dynamoDbAsyncTable: DynamoDbAsyncTable<GotchaObjectDetection>)
	: DetectionRequestDao {

	var log: Logger = LoggerFactory.getLogger(DetectionRequestDaoDynamo::class.java)

	override fun findItemByPkAndSk(pk: String, lsk: String): Mono<GotchaObjectDetection> {
		log.debug("Getting detectionRequest by primary partition key: [$pk] and local sort key: [$lsk]")
		return Mono.fromFuture(
				dynamoDbAsyncTable.getItem(Key.builder()
						.partitionValue(pk)
						.sortValue(lsk)
						.build()
				))
	}

	override fun findItemsByPk(pk: String): Flux<GotchaObjectDetection> {
		log.debug("Getting detectionRequest by primary partition key: [$pk]")
		val publisher = dynamoDbAsyncTable.query(QueryConditional.keyEqualTo(Key.builder()
				.partitionValue(pk)
				.build())).items()

		return Flux.from(publisher)
	}

	override fun updateItem(item: GotchaObjectDetection): Mono<GotchaObjectDetection> {
		return Mono.fromFuture(dynamoDbAsyncTable.updateItem(
				UpdateItemEnhancedRequest.builder(GotchaObjectDetection::class.java)
						.item(item)
						.build()
		))
	}


}