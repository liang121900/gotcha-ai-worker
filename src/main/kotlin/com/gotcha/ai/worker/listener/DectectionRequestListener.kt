package com.gotcha.ai.worker.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import com.gotcha.ai.worker.model.DetectionRequest
import com.gotcha.ai.worker.service.SqsService
import io.micronaut.context.annotation.Context
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.model.Message

@Singleton
@Context
class DectectionRequestListener(
    @Inject val sqsService: SqsService,
    @Inject val objectMapper: ObjectMapper
) {
    private val log: Logger = LoggerFactory.getLogger(DectectionRequestListener::class.java)

    init {
        log.info("Start listening!")
        listen()
    }

    fun listen() {
        sqsService.receiveMessage()
            .subscribe { message ->
                try {
                    log.info("message body: " + message.body())
                    objectMapper.readValue<DetectionRequest>(message.body())
                    var request: DetectionRequest = objectMapper.readValue(message.body())
                    log.info("parsed detection request: $request")
                } catch (e: MissingKotlinParameterException) {
                    log.error(e.stackTraceToString())
                } finally {
                    sqsService.deleteMessage(message)
                }
            }


    }
}