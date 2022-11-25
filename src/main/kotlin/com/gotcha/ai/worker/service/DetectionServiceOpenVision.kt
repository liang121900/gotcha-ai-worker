package com.gotcha.ai.worker.service

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import com.github.pgreze.process.unwrap
import com.gotcha.ai.worker.model.DeepVisionConfig
import jakarta.inject.Singleton
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

@Singleton
class DetectionServiceOpenVision(private val deepVisionConfig: DeepVisionConfig) : DetectionService {

	override fun detectRaw(imageLocation: String): Mono<String> {
		return mono {
			process(
					"python",
					"${deepVisionConfig.basePath}\\detect.py",
					"${deepVisionConfig.basePath}\\cfg\\yolo.cfg",
					"${deepVisionConfig.basePath}\\backup\\000105.weights",
					"${deepVisionConfig.basePath}\\data\\dog.jpg",
					// Capture stdout lines to do some operations after
					stdout = Redirect.CAPTURE,
					// Default value: prints to System.err
					stderr = Redirect.PRINT,

					).unwrap() // Fails if the resultCode != 0
		}.map { lines -> lines.joinToString("\n") }
				.onErrorResume { e -> Mono.just(e.message) }
	}
}