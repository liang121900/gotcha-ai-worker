package com.gotcha.ai.worker.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gotcha.ai.worker.constants.DetectionRequestConstants
import com.gotcha.ai.worker.constants.DetectionRequestStatus
import com.gotcha.ai.worker.dto.GotchaObjectDetection
import com.gotcha.ai.worker.model.DetectionRequest
import jakarta.inject.Inject
import jakarta.inject.Qualifier
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import java.time.ZonedDateTime
import java.util.*

/**
 * Convert to the GotchaObjectDetection dto entity
 *
 * Please check the example on doc/schema for details of the mapping
 */
@Mapper(componentModel = "jsr330")
abstract class GotchaObjectDetectionConverter {

	private var objectMapper: ObjectMapper? = null

	@Mapping(source = "requestId", target = "pk", qualifiedByName = ["requestIdToPk"])
	@Mapping(source = "status", target = "sk", qualifiedByName = ["statusToSk"])
	@Mapping(source = "createdDate", target = "created_date", qualifiedByName = ["zonedDateTimeToString"])
	@Mapping(source = "lastUpdatedDate", target = "last_updated_date", qualifiedByName = ["zonedDateTimeToString"])
	@Mapping(expression = "java(toJson(detectionRequest))", target = "data_1")
	abstract fun toGotchaObjectDetectionData(detectionRequest: DetectionRequest): GotchaObjectDetection

	@Mapping(source = "requestId", target = "pk", qualifiedByName = ["requestIdToPk"])
	@Mapping(target = "sk", source = "status", qualifiedByName = ["statusToSk"])
	@Mapping(source = "createdDate", target = "created_date", qualifiedByName = ["zonedDateTimeToString"])
	@Mapping(source = "lastUpdatedDate", target = "last_updated_date", qualifiedByName = ["zonedDateTimeToString"])
	@Mapping(target = "data_1", constant = "")
	abstract fun toGotchaObjectDetectionStatus(detectionRequest: DetectionRequest): GotchaObjectDetection

	@Named("requestIdToPk")
	fun requestIdToPk(requestId: String): String {
		return DetectionRequestConstants.REQUEST_ID_PREFIX + requestId
	}

	@Named("statusToSk")
	fun statusToSk(status: DetectionRequestStatus): String {
		return "status-$status"
	}

	// use objectMapper to convert date so that the format is consistent
	// TODO Define a datetime pattern instead of relying on objectMapper
	@Named("zonedDateTimeToString")
	fun zonedDateTimeToString(zonedDateTime: ZonedDateTime): String {
		return objectMapper?.writeValueAsString(zonedDateTime)?.trim('\"') ?: ""
	}

	fun toJson(detectionRequest: DetectionRequest): String {
		return objectMapper?.writeValueAsString(detectionRequest) ?: ""
	}

	@Inject
	fun setObjectMapper(@jakarta.inject.Named("dynamoObjectMapper") objectMapper: ObjectMapper) {
		this.objectMapper = objectMapper
	}

}