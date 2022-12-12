package com.gotcha.ai.worker.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import com.gotcha.ai.worker.constants.DetectionRequestStatus
import com.gotcha.ai.worker.converter.GotchaObjectDetectionConverter
import com.gotcha.ai.worker.model.DetectionRequest
import com.gotcha.ai.worker.service.DetectionService
import com.gotcha.ai.worker.service.FileService
import com.gotcha.ai.worker.service.SqsService
import io.micronaut.context.annotation.Context
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.sqs.model.Message
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Singleton
@Context
class DectectionRequestListener(
		@Inject val sqsService: SqsService,
		@Inject val objectMapper: ObjectMapper,
		@Inject val detectionService: DetectionService,
		@Inject val fileService: FileService,
		@Inject val gotchaObjectDetectionConverter: GotchaObjectDetectionConverter,
) {
	private val log: Logger = LoggerFactory.getLogger(DectectionRequestListener::class.java)

	init {
		log.info("Start listening!")
		listen()
	}

	fun listen() {
		sqsService.receiveMessage()
				.flatMap(this::mapMessageToDetectionRequest)
				.flatMap(this::updateStatusToProcessing)
				.flatMap { detectionRequest -> fileService.createInputFolder(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				.flatMap { detectionRequest -> fileService.createOutputFolder(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				.flatMap { detectionRequest -> fileService.downloadInput(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				//  .limitRate(2, 1) might want to do rate limit if the server does not have so much resource
				.flatMap { detectionRequest -> detectionService.generateDetectionResult(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				.flatMap { detectionRequest -> fileService.uploadResult(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				.flatMap { detectionRequest -> fileService.deleteInputFolder(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				.flatMap { detectionRequest -> fileService.deleteOutputFolder(detectionRequest).onErrorResume { e -> updateStatusToErrored(detectionRequest, e) } }
				.flatMap(this::updateStatusToProcessed)
				.subscribe({ detectionRequest ->
					sqsService.deleteMessage(detectionRequest.receiptHandle)
				}, { e -> log.error(e.stackTraceToString()) })
	}

	private fun mapMessageToDetectionRequest(message: Message): Mono<DetectionRequest> {
		return Mono.just(message)
				.map {
					var request = DetectionRequest()
					try {
						log.info("message body: " + message.body())
						objectMapper.readValue<DetectionRequest>(message.body())
						request = objectMapper.readValue(message.body())
						log.info("parsed detection request: $request")
					} catch (e: MissingKotlinParameterException) {
						log.error(e.stackTraceToString())
					} finally {
						request.receiptHandle = message.receiptHandle()
					}
					request
				}
	}

	private fun updateStatusToProcessing(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		detectionRequest.status = DetectionRequestStatus.PROCESSING
		detectionRequest.createdDate = ZonedDateTime.now()
		detectionRequest.lastUpdatedDate = ZonedDateTime.now()
		return detectionService.saveDetectionRequest(detectionRequest)
	}

	private fun updateStatusToProcessed(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		detectionRequest.status = DetectionRequestStatus.PROCESSED
		detectionRequest.createdDate = ZonedDateTime.now()
		detectionRequest.lastUpdatedDate = ZonedDateTime.now()
		return detectionService.saveDetectionRequest(detectionRequest)
	}

	private fun updateStatusToErrored(detectionRequest: DetectionRequest, e: Throwable): Mono<DetectionRequest> {
		detectionRequest.status = DetectionRequestStatus.ERRORED
		detectionRequest.errorMessage = e.stackTraceToString() ?: ""
		detectionRequest.lastUpdatedDate = ZonedDateTime.now()
		return detectionService.saveDetectionRequest(detectionRequest).flatMap { Mono.empty<DetectionRequest>() }
	}

}