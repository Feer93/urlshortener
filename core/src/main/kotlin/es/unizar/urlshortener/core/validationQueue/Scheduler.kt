package es.unizar.urlshortener.core.blockingQueue

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue


@Component
class Scheduler(
    val shortUrlRepository: ShortUrlRepositoryService,
    val validateUseCase: ValidateUseCase,
    val reachableUseCase: ReachableUrlUseCase,
) {

    @Autowired
    private val validationQueue: BlockingQueue<String>? = null

    // ? TODO: Hay alguna manera de que empiece al principio sin tenerla que llamar cada vez que llega una url ????
    @Async("validationExecutor")
    fun execute() {
        val url: String = validationQueue!!.take()

        val reachableResponse = reachableUseCase.isReachable(url)
        val validationResponse = validateUseCase.validate(url)
        val shortUrl: ShortUrl = shortUrlRepository.findByUrl(url)!!

        if(reachableResponse && validationResponse == ValidationResponse.VALID) {
                shortUrl.properties.safe = true
                // Marks shortURL as validated
                LOGGER.info("URl validada como segura y alcanzable")
        }
        shortUrl.properties.reachable = reachableResponse
        shortUrl.properties.validated = true
        shortUrlRepository.save(shortUrl)
    }
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Scheduler::class.java)
    }

}