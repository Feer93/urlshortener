package es.unizar.urlshortener.core.validationQueue

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue


@Component
class ValidationScheduler(
    val shortUrlRepository: ShortUrlRepositoryService,
    val validateUseCase: ValidateUseCase,
    val reachableUseCase: ReachableUrlUseCase,
) {

    @Autowired
    private val validationQueue: BlockingQueue<String>? = null

    @Async("validationExecutor")
    @Scheduled(fixedRate = 500)
    fun execute() {
        try {

            val url: String = validationQueue!!.take()

            val reachableResponse = reachableUseCase.isReachable(url)
            val validationResponse = validateUseCase.validate(url)
            val shortUrl: ShortUrl = shortUrlRepository.findByUrl(url)!!

            if (reachableResponse && validationResponse == ValidationResponse.VALID) {
                shortUrl.properties.safe = true
                // Marks shortURL as validated
                LOGGER.info("URl validada como segura y alcanzable")
            }
            shortUrl.properties.reachable = reachableResponse
            shortUrl.properties.validated = true
            shortUrlRepository.save(shortUrl)
        }catch (exception : InterruptedException){
            LOGGER.info("Proceso verificador interrumpido (cierre de servidor)")
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ValidationScheduler::class.java)
    }

}