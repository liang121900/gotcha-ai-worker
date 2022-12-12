package com.gotcha.ai.worker.service

import com.gotcha.ai.worker.config.properties.DeepVisionConfig
import com.gotcha.ai.worker.model.DetectionRequest
import io.micronaut.objectstorage.aws.AwsS3Operations
import io.micronaut.objectstorage.request.UploadRequest
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.io.File
import java.nio.file.*

@Singleton
class FileServiceDeepVision(
		private val deepVisionConfig: DeepVisionConfig,
		@Named("detection-input") private val inputBucketOperations: AwsS3Operations,
		@Named("detection-output") private val outputBucketOperations: AwsS3Operations,
) : FileService {

	var log: Logger = LoggerFactory.getLogger(FileServiceDeepVision::class.java)

	override fun createInputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return Mono.just(detectionRequest).doOnNext { it ->
			val inputPath: Path = getLocalInputFolderPath(it)
			Files.createDirectories(inputPath)
			log.debug("Created input directories $inputPath")
		}.doOnError { e -> detectionRequest.errorMessage.plus(e.message) }.onErrorResume { Mono.just(detectionRequest) }
	}

	override fun createOutputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return Mono.just(detectionRequest).doOnNext { it ->
			val outputPath: Path = getLocalOutputFolderPath(it)
			Files.createDirectories(outputPath)
			log.debug("Created output directories $outputPath")
		}.doOnError { e -> detectionRequest.errorMessage.plus(e.message) }.onErrorResume { Mono.just(detectionRequest) }
	}

	override fun deleteInputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return Mono.just(detectionRequest).doOnNext { it ->
			val inputPath: Path = getLocalInputFolderPath(it)
			Files.walk(inputPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			log.debug("Deleted input directory $inputPath")
		}.doOnError { e -> detectionRequest.errorMessage.plus(e.message) }.onErrorResume { Mono.just(detectionRequest) }
	}

	override fun deleteOutputFolder(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return Mono.just(detectionRequest).doOnNext { it ->
			val outputPath: Path = getLocalOutputFolderPath(it)
			Files.walk(outputPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			log.debug("Deleted output directory $outputPath")
		}.doOnError { e -> detectionRequest.errorMessage.plus(e.message) }.onErrorResume { Mono.just(detectionRequest) }
	}

	override fun downloadInput(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return Mono.just(detectionRequest).doOnNext { it ->
			val s3InputFilePath = getS3InputFilePath(it)
			val localInputFilePath = getLocalInputFilePath(it)
			Files.copy(inputBucketOperations.retrieve(s3InputFilePath).get().inputStream, localInputFilePath, StandardCopyOption.REPLACE_EXISTING)
			log.debug("Downloaded file [$s3InputFilePath] from s3 bucket to local on [$localInputFilePath]")
		}.doOnError { e -> detectionRequest.errorMessage.plus(e.message) }.onErrorResume { Mono.just(detectionRequest) }
	}


	override fun uploadResult(detectionRequest: DetectionRequest): Mono<DetectionRequest> {
		return Mono.just(detectionRequest).doOnNext { it ->
			val localOutputFilePath: Path = getLocalOutputFilePath(it)
			val s3OutputPath: String = getS3OutputPath(it)
			log.debug("Going to upload file to $s3OutputPath")
			outputBucketOperations.upload(UploadRequest.fromPath(localOutputFilePath, getS3OutputPrefix(it)))
			detectionRequest.outputPath = s3OutputPath
			log.debug("Uploaded [$localOutputFilePath] to s3 bucket on [$s3OutputPath]")
		}.doOnError { e -> detectionRequest.errorMessage.plus(e.message) }.onErrorResume { Mono.just(detectionRequest) }

	}

	override fun getLocalInputFolderPath(detectionRequest: DetectionRequest): Path = Paths.get("${deepVisionConfig.basePath}").resolve("input").resolve(detectionRequest.requestId)
	override fun getLocalInputFilePath(detectionRequest: DetectionRequest): Path = Paths.get("${deepVisionConfig.basePath}").resolve("input").resolve(detectionRequest.requestId).resolve(detectionRequest.fileName)
	override fun getLocalOutputFolderPath(detectionRequest: DetectionRequest): Path = Paths.get("${deepVisionConfig.basePath}").resolve("output").resolve(detectionRequest.requestId)
	override fun getLocalOutputFilePath(detectionRequest: DetectionRequest): Path = getLocalOutputFolderPath(detectionRequest).resolve("predictions.jpg")
	override fun getS3InputFilePath(detectionRequest: DetectionRequest): String = detectionRequest.inputPath
	override fun getS3OutputPrefix(detectionRequest: DetectionRequest): String = detectionRequest.requestId
	override fun getS3OutputPath(detectionRequest: DetectionRequest): String = getS3OutputPrefix(detectionRequest) + "/predictions.jpg"

}