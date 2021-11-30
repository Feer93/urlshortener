package es.unizar.urlshortener

import es.unizar.urlshortener.core.QrRepositoryService
import es.unizar.urlshortener.core.usecases.*
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

/**
 * Wires use cases with service implementations, and services implementations with repositories.
 *
 * **Note**: Spring Boot is able to discover this [Configuration] without further configuration.
 */
@Configuration
@EnableAsync
class ApplicationConfiguration(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val qrEntityRepository: QrEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
    @Autowired val meterRegistry: MeterRegistry
) {
    @Bean
    fun clickRepositoryService() = ClickRepositoryServiceImpl(clickEntityRepository)

    @Bean
    fun shortUrlRepositoryService() = ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)

    @Bean
    fun qrRepositoryService() = QrRepositoryServiceImpl(qrEntityRepository)

    @Bean
    fun validatorService() = ValidatorServiceImpl()

    @Bean
    fun hashService() = HashServiceImpl()

    @Bean
    fun infoRepositoryService() = InfoRepositoryServiceImpl(shortUrlEntityRepository, clickEntityRepository)

    @Bean
    fun redirectUseCase() = RedirectUseCaseImpl(shortUrlRepositoryService())

    @Bean
    fun logClickUseCase() = LogClickUseCaseImpl(clickRepositoryService(), meterRegistry)

    @Bean
    fun createShortUrlUseCase() = CreateShortUrlUseCaseImpl(shortUrlRepositoryService(), validatorService(),
        hashService(), meterRegistry)

    @Bean
    fun validateUseCase() = ValidateUseCaseImpl(meterRegistry)

    @Bean
    fun recoverInfoUseCase() = RecoverInfoUseCaseImpl(infoRepositoryService())

    //@Bean
    //fun meterRegistry() = meterRegistry

    @Bean
    fun createQrUseCase() = CreateQrUseCaseImpl(qrRepositoryService(), meterRegistry)
    
    @Bean
    fun timedAspect() = TimedAspect(meterRegistry)
}
