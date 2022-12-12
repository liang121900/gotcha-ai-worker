package com.gotcha.ai.worker.controller

import com.gotcha.ai.worker.model.DetectionRequest
import com.gotcha.ai.worker.service.DetectionService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.objectstorage.aws.AwsS3Operations
import io.micronaut.objectstorage.request.UploadRequest
import jakarta.inject.Named
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.InputStream

@Controller("/detections")
class DetectionController(
		@Named("detection-input") private val inputBucketOperations: AwsS3Operations,
		@Named("detection-output") private val outputBucketOperations: AwsS3Operations,
		private val detectionService: DetectionService
) {

	@Get("/{detectionRequestId}")
	fun getDetectionRequest(detectionRequestId: String): Mono<HttpResponse<DetectionRequest>> {
		return detectionService.findDetectionRequestByRequestId(detectionRequestId)
				.map { request -> HttpResponse.ok(request) }
	}
}