package com.gotcha.ai.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class JsonConfig {

    @Singleton
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule()
    }
}