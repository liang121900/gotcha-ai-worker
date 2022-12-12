package com.gotcha.ai.worker.service

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import com.github.pgreze.process.unwrap
import com.gotcha.ai.worker.config.properties.DeepVisionConfig
import com.gotcha.ai.worker.constants.DetectionRequestStatus
import com.gotcha.ai.worker.converter.DetectionRequestConverter
import com.gotcha.ai.worker.converter.GotchaObjectDetectionConverter
import com.gotcha.ai.worker.dao.DetectionRequestDao
import com.gotcha.ai.worker.model.DetectionRequest
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.nio.file.Path

@Singleton
class DetectionServiceDeepVision(
		private val deepVisionConfig: DeepVisionConfig,
		private val fileService: FileService,
		private val detectionRequestDao: DetectionRequestDao,
		private val gotchaObjectDetectionConverter: GotchaObjectDetectionConverter,
		private val detectionRequestConverter: DetectionRequestConverter,
) : DetectionService {

	var log: Logger = LoggerFactory.getLogger(DetectionServiceDeepVision::class.java)


	/**
	 *	The python script is modified from YOLO,
	 *	Details on https://pjreddie.com/darknet/yolo/
	 */
	override fun generateDetectionResult(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return mono {
			process(
					"python",
					"detect.py",
					/*"cfg\\yolo.cfg",*/
					Path.of("cfg").resolve("yolo.cfg").toString(),
					/*"backup\\000105.weights",*/
					Path.of("backup").resolve("000105.weights").toString(),
					/*"input\\${detectionRequest.requestId}\\${detectionRequest.fileName}",*/
					Path.of("input").resolve(detectionRequest.requestId).resolve(detectionRequest.fileName).toString(),
					/*"output\\${detectionRequest.requestId}\\predictions.jpg",*/
					Path.of("output").resolve(detectionRequest.requestId).resolve("predictions.jpg").toString(),
					// Capture stdout lines to do some operations after
					stdout = Redirect.Consume { flow -> flow.onEach(log::debug).collect() },
					// Default value: prints to System.err
					stderr = Redirect.Consume { flow -> flow.onEach(log::error).collect() },
					directory = File(deepVisionConfig.basePath),
					/*consumer = {line -> log.debug(line)}*/
			).unwrap()
		} // Fail if the resultCode != 0
/*				.doOnError { e ->
					log.error("Error on running detection process: ", e.stackTraceToString())
					detectionRequest.errorMessage = e.stackTraceToString()
					detectionRequest.status = DetectionRequestStatus.ERRORED
				}
				.onErrorResume { e -> Mono.just(e.message ?: "") }*/
				.map { _ -> detectionRequest }
	}

	/**
	 * Find the requests by id and return the one with latest lastUpdatedDate.
	 */
	override fun findDetectionRequestByRequestId(requestId: String): Mono<DetectionRequest> {
		val gotchaObjectDetection = gotchaObjectDetectionConverter.toGotchaObjectDetectionData(DetectionRequest(requestId = requestId))
		return detectionRequestDao.findItemsByPk(gotchaObjectDetection.pk)
				.map(detectionRequestConverter::toDetectionRequest)
				.sort { r1, r2 -> r1.lastUpdatedDate.compareTo(r2.lastUpdatedDate) }
				.last()
				.onErrorResume { Mono.empty<DetectionRequest>() }
	}

	override fun saveDetectionRequest(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return detectionRequestDao.updateItem(gotchaObjectDetectionConverter.toGotchaObjectDetectionData(detectionRequest))
				.map(detectionRequestConverter::toDetectionRequest)
	}

}