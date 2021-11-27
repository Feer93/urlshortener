package es.unizar.urlshortener.core.usecases

import com.maxmind.geoip2.DatabaseReader
import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ClickRepositoryService
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import java.net.InetAddress
import java.time.OffsetDateTime

/**
 * Log that somebody has requested the redirection identified by a key.
 *
 * **Note**: This is an example of functionality.
 */
interface LogClickUseCase {
    fun logClick(key: String, data: ClickProperties)
}

/**
 * Implementation of [LogClickUseCase].
 */
open class LogClickUseCaseImpl(
    private val clickRepository: ClickRepositoryService,
    private val meterRegistry: MeterRegistry,
    private val databaseReader: DatabaseReader?
) : LogClickUseCase {

    private var redirectionCounter: Counter = Counter.builder("user.action").
        tag("type", "clickedURL").
        description("Number of redirections").
        register(meterRegistry)

    /*
    @Autowired
    fun initMetrics(meterRegistry: MeterRegistry){
        redirectionCounter = Counter.builder("user.action").tag("type", "clickedURL")
            .description("Number of redirections").register(meterRegistry)
        //redirectionCounter = meterRegistry.counter("user.action", "type", "clickedURL")
    }*/
    @Async
    open fun updateMetrics(){
        redirectionCounter.increment()
        redirectionCounter = Counter.builder("user.action").tag("type", "click")
            .description("Number of redirections").register(meterRegistry)
    }

    private fun getCountry(ip: String): String?{
        val s: String? = try {
            databaseReader?.country(InetAddress.getByName(ip))?.country?.name;
        } catch (e: Exception){
            null;
        }
        return s;
    }

    override fun logClick(key: String, data: ClickProperties) {
        val cl = Click(
            hash = key,
            properties = ClickProperties(
                ip = data.ip,
                country = data.ip?.let { getCountry(it) },
                browser = data.browser,
                created = OffsetDateTime.now()
            )
        )
        updateMetrics()
        clickRepository.save(cl)
    }
}
