package com.gotcha.ai.worker.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.gotcha.ai.worker.constants.DetectionRequestConstants
import com.gotcha.ai.worker.constants.DetectionRequestStatus
import com.gotcha.ai.worker.dto.GotchaObjectDetection
import com.gotcha.ai.worker.model.DetectionRequest
import jakarta.inject.Inject
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named

@Mapper(componentModel = "jsr330")
abstract class DetectionRequestConverter {

	@Inject
	var objectMapper: ObjectMapper? = null

	fun toDetectionRequest(gotchaObjectDetection: GotchaObjectDetection): DetectionRequest {
		return objectMapper?.readValue(gotchaObjectDetection.data_1, DetectionRequest::class.java) ?: DetectionRequest()
	}
}
