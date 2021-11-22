package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder().build()
    }
}
