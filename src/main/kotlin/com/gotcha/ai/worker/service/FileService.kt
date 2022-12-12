package com.gotcha.ai.worker.service

import com.gotcha.ai.worker.model.DetectionRequest
import reactor.core.publisher.Mono
import java.nio.file.Path

interface FileService {
	fun createInputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest>;
	fun createOutputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest>;
	fun deleteInputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest>;
	fun deleteOutputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest>;
	fun uploadResult(detectionRequest: DetectionRequest): Mono<DetectionRequest>;
	fun downloadInput(detectionRequest: DetectionRequest): Mono<DetectionRequest>;

	fun getLocalInputFolderPath(detectionRequest: DetectionRequest): Path
	fun getLocalInputFilePath(detectionRequest: DetectionRequest): Path
	fun getLocalOutputFolderPath(detectionRequest: DetectionRequest): Path
	fun getLocalOutputFilePath(detectionRequest: DetectionRequest): Path
	fun getS3InputFilePath(detectionRequest: DetectionRequest): String
	fun getS3OutputPrefix(detectionRequest: DetectionRequest): String
	fun getS3OutputPath(detectionRequest: DetectionRequest): String
}