package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.ValidateUseCase
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component("Validation service health")
class ValidationServiceHealthIndicator(
    val validator: ValidateUseCase
): ReactiveHealthIndicator {

    private val service = "Google Safebrowsing health"

    override fun health(): Mono<Health> {
        return isRunningValidationService()
    }

    fun isRunningValidationService(): Mono<Health> {

        return if (validator.isSafeWithoutCounters("https://www.google.com/")) {
            Mono.just(
                Health.Builder().up().withDetail("Service", service).build()
            )

        } else {
            Mono.just(
                Health.Builder().down().withDetail("Service", service).build()
            )

        }
    }

}