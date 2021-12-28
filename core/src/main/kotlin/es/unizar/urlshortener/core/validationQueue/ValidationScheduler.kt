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
            LOGGER.info("Waiting for validation: ")
            val url: String = validationQueue!!.take()
            LOGGER.info("Validating an URL")
            val shortUrl: ShortUrl = shortUrlRepository.findByUrl(url)!!

            /*Validates the url */
            val isReachable = reachableUseCase.isReachable(url)
            val isSafe = validateUseCase.isSafe(url)

            shortUrl.properties.safe = isSafe
            shortUrl.properties.reachable = isReachable
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