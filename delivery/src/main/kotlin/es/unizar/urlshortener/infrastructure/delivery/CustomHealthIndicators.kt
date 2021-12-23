package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.ValidateUseCase
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component("Validation service health")
class ValidationServiceHealthIndicator(
    //webBuilder: WebClient.Builder,
    val validator: ValidateUseCase
): ReactiveHealthIndicator {

    private val service = "Google Safebrowsing health"
    //private val webClient = webBuilder.build()

    override fun health(): Mono<Health> {
        return isRunningValidationService()
    }

    fun isRunningValidationService(): Mono<Health> {
        /*
        return webClient.get()
            .uri("http://localhost:8080/info").retrieve().bodyToMono(String::class.java)
            .map { Health.Builder().up().withDetail("Service", service).build() }
            .onErrorResume { exception: Throwable? ->
                Mono.just(
                    Health.Builder().down(exception).build()
                )
            }

         */

        return if (validator.isSafe("https://www.google.com/")) {
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