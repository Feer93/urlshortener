package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.RecoverInfoUseCase
import org.springframework.boot.actuate.health.Health

import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info

import org.springframework.boot.actuate.info.InfoContributor


@Component
class RedirectionInfoContributor(
    val info: RecoverInfoUseCase
) : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("Top 100 clicked shortened URl", info.recoverTopKRedirection(100))
    }
}

@Component
class ShortenedURLInfoContributor(
    val info: RecoverInfoUseCase
) : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("Top 100 hosts to have a URl shortened", info.recoverTopKShortenedURL(100))
    }
}

@Component
class CountInfoContributor(
    val info: RecoverInfoUseCase
) : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        val countInformation: MutableMap<String, Long> = HashMap()
        countInformation["Shortened URL"] = info.countURL()
        countInformation["Clicks in shortened URL"] = info.countRedirection()
        builder.withDetail("Number of:", countInformation)
    }
}