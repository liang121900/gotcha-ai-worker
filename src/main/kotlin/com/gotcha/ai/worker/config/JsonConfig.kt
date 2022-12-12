package com.gotcha.ai.worker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.*

@Factory
class JsonConfig {

	@Singleton
	@Named("dynamoObjectMapper")
	fun objectMapper(): ObjectMapper {
		val objectMapper = ObjectMapper().registerKotlinModule()
		objectMapper.registerModule(JavaTimeModule())
		objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"))
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
/*		objectMapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
		objectMapper.disable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE)
	*/	return objectMapper
	}
}