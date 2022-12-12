package com.gotcha.ai.worker.config.aws

import com.gotcha.ai.worker.config.properties.DynamoConfig
import com.gotcha.ai.worker.dto.GotchaObjectDetection
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException
import java.net.URI


@Factory
class DynamoClientConfig(val dynamoConfig: DynamoConfig) : AWSClient() {

	var log: Logger = LoggerFactory.getLogger(DynamoClientConfig::class.java)

	@Singleton
	fun dynamoDBAsyncClient(): DynamoDbAsyncClient {
		var clientBuilder = DynamoDbAsyncClient.builder()
		// For connecting to localstack
		if (serviceEndpoint != null) {
			clientBuilder.endpointOverride(URI(serviceEndpoint))
			clientBuilder.region(Region.US_EAST_1)
		}
		return clientBuilder.build()
	}

	@Singleton
	fun dynamoDbEnhancedAsyncClient(): DynamoDbEnhancedAsyncClient {
		return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(dynamoDBAsyncClient()).build()
	}

	@Singleton
	fun gotchaObjectDetectionTable(): DynamoDbAsyncTable<GotchaObjectDetection> {
		val mappedTable = dynamoDbEnhancedAsyncClient().table(dynamoConfig.tableName,
				TableSchema.fromBean(GotchaObjectDetection::class.java))
		if (dynamoConfig.autoCreate && !tableExists(dynamoConfig.tableName, dynamoDBAsyncClient())) {
			log.info("Creating table ${dynamoConfig.tableName} as auto-create is enable and the table [${dynamoConfig.tableName}] does not exist.")
			mappedTable.createTable().join()
			dynamoDBAsyncClient().waiter()
					.waitUntilTableExists { b -> b.tableName(dynamoConfig.tableName) }
					.get()
		}
		return mappedTable
	}

	/**
	 *
	 * Check if the dynamoDb table exists by describing the table by name
	 * and catching the [ResourceNotFoundException] exception
	 *
	 */
	private fun tableExists(tableName: String, dynamoDbAsyncClient: DynamoDbAsyncClient): Boolean {
		var result: Boolean = true
		dynamoDbAsyncClient.describeTable(DescribeTableRequest.builder()
				.tableName(tableName)
				.build()
		).exceptionally { e ->
			if (e != null) {
				if (e.cause is ResourceNotFoundException)
					result = false
				else
					throw e
			}
			DescribeTableResponse.builder().build()
		}.join()
		return result
	}

}