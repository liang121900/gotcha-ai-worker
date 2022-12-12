package com.gotcha.ai.worker.dao

import com.gotcha.ai.worker.dto.GotchaObjectDetection
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface DetectionRequestDao {

	fun findItemByPkAndSk(pk: String, sk: String): Mono<GotchaObjectDetection>

	fun updateItem(item: GotchaObjectDetection): Mono<GotchaObjectDetection>
	fun findItemsByPk(pk: String): Flux<GotchaObjectDetection>
}