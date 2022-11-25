package com.gotcha.ai.worker.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import com.gotcha.ai.worker.model.DetectionRequest
import com.gotcha.ai.worker.service.DetectionService
import com.gotcha.ai.worker.service.SqsService
import io.micronaut.context.annotation.Context
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Singleton
@Context
class DectectionRequestListener(
		@Inject val sqsService: SqsService,
		@Inject val objectMapper: ObjectMapper,
		@Inject val detectionService: DetectionService
) {
	private val log: Logger = LoggerFactory.getLogger(DectectionRequestListener::class.java)

	init {
		log.info("Start listening!")
		listen()
	}

	fun listen() {
		sqsService.receiveMessage()
				.map { message ->
					var request = DetectionRequest()
					try {
						log.info("message body: " + message.body())
						objectMapper.readValue<DetectionRequest>(message.body())
						request = objectMapper.readValue(message.body())
						log.info("parsed detection request: $request")
					} catch (e: MissingKotlinParameterException) {
						log.error(e.stackTraceToString())
					}finally {
						request.receiptHandle=message.receiptHandle()
					}
					request
				}
				.flatMap { detectionRequest ->
					Mono.just(detectionRequest)
							.map {request->
								val result = detectionService.detectRaw(detectionRequest.receiptHandle).block()
								log.info("Detection result: $result")
								request
							}
				}
				.subscribe { detectionRequest ->
						sqsService.deleteMessage(detectionRequest.receiptHandle)
				}
	}
}