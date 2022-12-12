package com.gotcha.ai.worker

import io.micronaut.runtime.Micronaut.*

fun main(args: Array<String>) {
	build(*args)
			.eagerInitSingletons(true)
			.start()
}
