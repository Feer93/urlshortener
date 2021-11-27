package es.unizar.urlshortener

import es.unizar.urlshortener.infrastructure.delivery.HashServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.ValidatorServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.*
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.GeoIp2Exception
import es.unizar.urlshortener.core.usecases.*
import java.io.File

import java.io.IOException
import org.springframework.cache.concurrent.ConcurrentMapCacheManager

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling


/**
 * Wires use cases with service implementations, and services implementations with repositories.
 *
 * **Note**: Spring Boot is able to discover this [Configuration] without further configuration.
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableCaching
class ApplicationConfiguration(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
    @Autowired val meterRegistry: MeterRegistry
) {
    @Bean
    fun clickRepositoryService() = ClickRepositoryServiceImpl(clickEntityRepository)

    @Bean
    fun shortUrlRepositoryService() = ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)

    @Bean
    fun validatorService() = ValidatorServiceImpl()

    @Bean
    fun hashService() = HashServiceImpl()

    @Bean
    fun infoRepositoryService() = InfoRepositoryServiceImpl(shortUrlEntityRepository, clickEntityRepository)

    @Bean
    @Throws(IOException::class, GeoIp2Exception::class)
    fun databaseReader(): DatabaseReader? = DatabaseReader.Builder(
        //File("./src/main/resources/GeoLite2-Country.mmdb")).build()
        File("/home/psoft/Documentos/IngWeb/urlshortener/app/src/main/resources/GeoLite2-Country.mmdb")).build()

    @Bean
    fun redirectUseCase() = RedirectUseCaseImpl(shortUrlRepositoryService())

    @Bean
    fun logClickUseCase() = LogClickUseCaseImpl(clickRepositoryService(), meterRegistry, databaseReader())

    @Bean
    fun createShortUrlUseCase() = CreateShortUrlUseCaseImpl(shortUrlRepositoryService(), validatorService(),
        hashService(), meterRegistry, databaseReader())

    @Bean
    fun validateUseCase() = ValidateUseCaseImpl()

    @Bean
    fun recoverInfoUseCase() = RecoverInfoUseCaseImpl(infoRepositoryService())

    //@Bean
    //fun meterRegistry() = meterRegistry

    @Bean
    fun timedAspect() = TimedAspect(meterRegistry)

    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(
        "TopKShortenedURL", "countURL", "countRedirection", "TopKRedirection")

}