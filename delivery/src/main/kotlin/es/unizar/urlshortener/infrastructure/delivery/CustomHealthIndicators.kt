package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.boot.actuate.health.Health

import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component


@Component("Validation service health")
class ValidationServiceHealthIndicator : HealthIndicator {
    private val service = "Validation service health"

    override fun health(): Health {
        return if (!isRunningValidationService()) {
            Health.down().withDetail(service, "DOWN").build()
        } else Health.up().withDetail(service, "UP").build()
    }

    fun isRunningValidationService(): Boolean = true
}