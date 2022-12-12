package com.gotcha.ai.worker.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.gotcha.ai.worker.config.health.DectectionRequestListenerHealthIndicator
import com.gotcha.ai.worker.config.properties.SqsQueueConfig
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.time.Duration
import java.util.function.Function

@Singleton
class SqsServiceGotchaDetection(
        private val sqsQueueConfig: SqsQueueConfig,
        @Inject @Named("gotchaAiDetectionRequestSqsAsyncClient") val sqsAsyncClient: SqsAsyncClient,
        @Inject val objectMapper: ObjectMapper
) : SqsService {
    var log: Logger = LoggerFactory.getLogger(SqsServiceGotchaDetection::class.java)

    override fun deleteMessage(message: Message) {
        sqsAsyncClient.deleteMessage(
            DeleteMessageRequest.builder()
                .queueUrl(sqsQueueConfig.queueUrl)
                .receiptHandle(message.receiptHandle())
                .build()
        ).thenAccept { _ -> log.info("deleted message with handle ${message.receiptHandle()}") }
    }

    override fun deleteMessage(receiptHandle: String) {
        sqsAsyncClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(sqsQueueConfig.queueUrl)
                        .receiptHandle(receiptHandle)
                        .build()
        ).thenAccept { _ -> log.info("deleted message with handle $receiptHandle") }
    }

    override fun receiveMessage(): Flux<Message> {
        return Mono.fromFuture {
            val receiveMessageRequest = ReceiveMessageRequest.builder()
                .maxNumberOfMessages(5)
                .queueUrl(sqsQueueConfig.queueUrl)
                .waitTimeSeconds(10)
                .visibilityTimeout(30)
                .build()
            sqsAsyncClient.receiveMessage(receiveMessageRequest)
        }
            .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)))
            .doOnError(Exception::class.java) { e ->
                log.error(e.stackTraceToString())
                DectectionRequestListenerHealthIndicator.setStatusDown()
            }
            .doOnNext {
                DectectionRequestListenerHealthIndicator.setStatusUp()
            }
            .onErrorResume { e -> Mono.empty() }
            .repeat()
            .map(ReceiveMessageResponse::messages)
            .map { Flux.fromIterable(it) }
            .flatMap(Function.identity())

    }

    override fun getQueueAttributes(): Mono<GetQueueAttributesResponse> {
        return Mono.fromFuture<GetQueueAttributesResponse?> {
            val request = GetQueueAttributesRequest.builder()
                .queueUrl(sqsQueueConfig.queueUrl)
                .build()
            sqsAsyncClient.getQueueAttributes(request)
        }
    }

}