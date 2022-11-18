package com.gotcha.ai.worker.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse
import software.amazon.awssdk.services.sqs.model.Message

interface SqsService {
    open fun deleteMessage(message: Message)
    open fun receiveMessage(): Flux<Message>
    open fun getQueueAttributes(): Mono<GetQueueAttributesResponse>
}