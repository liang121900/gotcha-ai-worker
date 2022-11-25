package com.gotcha.ai.worker.model

data class DetectionResult(
		var xAxis1: Double,
		var xAxis2: Double,
		var yAxis1: Double,
		var yAxis2: Double,
		var type: String
)