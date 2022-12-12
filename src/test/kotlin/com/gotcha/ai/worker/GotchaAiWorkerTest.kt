package com.gotcha.ai.worker

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import jakarta.inject.Inject
import org.junit.jupiter.api.Disabled

@MicronautTest
@Disabled
class GotchaAiWorkerTest {

	@Inject
	lateinit var application: EmbeddedApplication<*>

	@Test
	fun testItWorks() {
		Assertions.assertTrue(application.isRunning)
	}

}
