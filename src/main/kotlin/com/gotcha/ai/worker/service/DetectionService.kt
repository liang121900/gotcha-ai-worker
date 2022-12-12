package com.gotcha.ai.worker.service

import com.gotcha.ai.worker.model.DetectionRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DetectionService {

	fun generateDetectionResult(detectionRequest: DetectionRequest): Mono<DetectionRequest>

	fun saveDetectionRequest(detectionRequest: DetectionRequest): Mono<DetectionRequest>
	fun findDetectionRequestByRequestId(requestId: String): Mono<DetectionRequest>
}