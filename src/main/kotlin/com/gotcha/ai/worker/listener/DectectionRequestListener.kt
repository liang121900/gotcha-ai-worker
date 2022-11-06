package com.gotcha.ai.worker.listener

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Property
import io.micronaut.jms.annotations.JMSListener
import io.micronaut.jms.annotations.Message
import io.micronaut.jms.annotations.Queue
import io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME
import io.micronaut.messaging.annotation.MessageBody
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable


@JMSListener(CONNECTION_FACTORY_BEAN_NAME)
class DectectionRequestListener {
    var log: Logger = LoggerFactory.getLogger(DectectionRequestListener::class.java)

    @Queue(value = "\${gotcha-ai.sqs.detection-request-queue.queue-name}", concurrency = "1-10")
    fun receive(
            @MessageBody body: Map<String, Serializable>,
            @Message message: javax.jms.Message,
    ) {
        log.info("received sqs body: [$body]")
        log.info("received sqs message: [$message]")
    }

}