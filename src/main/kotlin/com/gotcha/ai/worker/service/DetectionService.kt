package com.gotcha.ai.worker.service

import com.gotcha.ai.worker.model.DetectionResult
import reactor.core.publisher.Mono

interface DetectionService {
	open fun detectRaw(imageLocation: String): Mono<String>
}