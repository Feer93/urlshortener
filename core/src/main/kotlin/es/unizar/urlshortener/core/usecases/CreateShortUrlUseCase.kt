package es.unizar.urlshortener.core.usecases

import com.maxmind.geoip2.DatabaseReader
import es.unizar.urlshortener.core.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import java.net.InetAddress
import java.time.OffsetDateTime
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
    private val meterRegistry: MeterRegistry,
    private val databaseReader: DatabaseReader?
) : CreateShortUrlUseCase {

    /**
     * Counter to count the number of created shortened URL
     */
    private var shortenerCounter: Counter = Counter.builder("user.action").
        tag("type", "createShortenedURL").
        register(meterRegistry)


    /**
     * Gauge that stores the length of the last URL sent to shorten
     */
    private var lastMsgLength: AtomicInteger = meterRegistry.
        gauge("shortener.last.url.length", AtomicInteger())!!

    /**
     *  Increment in 1 the value of the counter and update the value that stores
     *  the length of the last URL sent to shorten
     */
    open fun updateMetrics(n: Int){
        shortenerCounter.increment()
        lastMsgLength.set(n)
    }

    private fun getCountry(ip: String): String?{
        val s: String? = try {
            databaseReader?.country(InetAddress.getByName(ip))?.country?.name;
        } catch (e: Exception){
            null;
        }
        return s;
    }

    override fun create(url: String, data: ShortUrlProperties): ShortUrl =

        if (validatorService.isValid(url)) {
            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    reachable = data.reachable,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    country = data.ip?.let { getCountry(it) },
                    browser = data.browser,
                    created = OffsetDateTime.now()
                )
            )

            updateMetrics(url.length)
            shortUrlRepository.save(su)
        } else {
            throw InvalidUrlException(url)
        }

}
