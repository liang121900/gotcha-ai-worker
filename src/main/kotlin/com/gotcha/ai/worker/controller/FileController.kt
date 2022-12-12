package com.gotcha.ai.worker.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.objectstorage.aws.AwsS3ObjectStorageEntry
import io.micronaut.objectstorage.aws.AwsS3Operations
import io.micronaut.objectstorage.request.UploadRequest
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.InputStream
import java.util.*


/**
This controller is mostly for testing purpose, when you need to set up some test images on s3 bucket.
 */
@Controller("/files")
class FileController(
		@Named("detection-input") private val inputBucketOperations: AwsS3Operations,
		@Named("detection-output") private val outputBucketOperations: AwsS3Operations,
) {

	private val log: Logger = LoggerFactory.getLogger(FileController::class.java)

	/**
	 * To get the upload image to be processed
	 */
	@Get("input", produces = [MediaType.TEXT_EVENT_STREAM])
	fun getInputFile(@QueryValue objectKey: String): HttpResponse<Mono<InputStream>> {
		return HttpResponse.ok(Mono.just(inputBucketOperations.retrieve(objectKey).get().inputStream))
	}

	/**
	 * For uploading a image for further processing
	 */
	@Post(value = "input", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.APPLICATION_JSON])
	fun postInputFile(file: ByteArray, @QueryValue objectKey: String): Flux<HttpResponse<Any>> {
		return Flux.just(inputBucketOperations.upload(UploadRequest.fromBytes(file, objectKey)))
				.map { request -> HttpResponse.created(request) }
	}

	@Get("output", produces = [MediaType.TEXT_EVENT_STREAM])
	fun getOutputFile(@QueryValue objectKey: String): Mono<MutableHttpResponse<InputStream>> {
		return Mono.just(outputBucketOperations.retrieve(objectKey))
				.flatMap { it ->
					if (it.isPresent) Mono.just(it.get()) else Mono.empty()
				}
				.map { it -> HttpResponse.ok(it.inputStream) }
				.switchIfEmpty(Mono.just(HttpResponse.notFound()))
	}
}