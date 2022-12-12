package com.gotcha.ai.worker.model

import com.gotcha.ai.worker.constants.DetectionRequestStatus
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class DetectionRequest(
		val requestId: String = "",
		val fileName: String="",
		var inputPath: String = "",
		var outputPath: String = "",
		var receiptHandle: String = "", // For deleting the sqs message
		var errorMessage: String = "",
		var createdDate: ZonedDateTime = ZonedDateTime.now(),
		var lastUpdatedDate: ZonedDateTime = ZonedDateTime.now(),
		var status: DetectionRequestStatus = DetectionRequestStatus.CREATED
)