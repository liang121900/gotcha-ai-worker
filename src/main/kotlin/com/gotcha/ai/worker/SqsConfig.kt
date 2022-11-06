package com.gotcha.ai.worker

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import com.amazonaws.ClientConfiguration
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import io.micronaut.jms.sqs.configuration.properties.SqsConfigurationProperties
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.jms.ConnectionFactory


@Factory
class SqsConfig {
    var log: Logger = LoggerFactory.getLogger(SqsConfig::class.java)
    val CONNECTION_FACTORY_BEAN_NAME = "sqsJmsConnectionFactory"

    @Singleton
    fun detectionRequestsSqsClient(environment: Environment): AmazonSQS? {
        log.info("environemnt: ${environment.activeNames}")
        var clientBuilder = AmazonSQSClientBuilder
                .standard()
        if ("local" in (environment.activeNames)) {
            clientBuilder.withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
                            "us-east-1"))
        } else {
            clientBuilder.withRegion(Regions.US_EAST_2)
        }
        return clientBuilder.build()
    }


    @Singleton
    fun sqsJmsConnectionFactory(config: SqsConfigurationProperties,
                                detectionRequestsSqsClient: AmazonSQS?): ConnectionFactory? {
        log.info("created ConnectionFactory bean $CONNECTION_FACTORY_BEAN_NAME")
        return SQSConnectionFactory(
                ProviderConfiguration().withNumberOfMessagesToPrefetch(config.numberOfMessagesToPrefetch),
                detectionRequestsSqsClient)
    }
}