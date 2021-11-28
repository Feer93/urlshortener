package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.atomic.AtomicInteger

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface CreateShortUrlUseCase {
    fun create(url: String, data: ShortUrlProperties): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
open class CreateShortUrlUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validatorService: ValidatorService,
    private val hashService: HashService,
    private val meterRegistry: MeterRegistry
) : CreateShortUrlUseCase {

    private var shortenerCounter: Counter = Counter.builder("user.action").
        tag("type", "createShortenedURL").
        description("Number of shortened URLs created").
        register(meterRegistry)

    private var lastMsgLength: AtomicInteger = meterRegistry.
        gauge("shortener.last.url.length", AtomicInteger())!!

    /*
    @Autowired
    fun initMetrics(meterRegistry: MeterRegistry){
        shortenerCounter = Counter.builder("user.action").tag("type", "createShortenedURL")
            .description("Number of shortened URLs created").register(meterRegistry)

        //shortenerCounter = meterRegistry.counter("user.action", "type", "shortenedURL")

        lastMsgLength = meterRegistry.gauge("shortener.last.url.length", AtomicInteger())!!
    }*/
    @Async
    open fun updateMetrics(n: Int){
        shortenerCounter.increment()
        lastMsgLength.set(n)
    }

    override fun create(url: String, data: ShortUrlProperties): ShortUrl =
        if (validatorService.isValid(url)) {
            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    ip = data.ip,
                    sponsor = data.sponsor
                )
            )
            updateMetrics(url.length)
            shortUrlRepository.save(su)
        } else {
            throw InvalidUrlException(url)
        }
}
