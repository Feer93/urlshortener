package es.unizar.urlshortener.infrastructure.delivery

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.maxmind.geoip2.DatabaseReader
import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.*
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.InetAddress
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.http.HttpServletRequest
import java.util.concurrent.BlockingQueue
import es.unizar.urlshortener.core.blockingQueue.Scheduler

/**
 * The specification of the controller.
 */
interface UrlShortenerController {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Void>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    /**
     * Returns the URI associated to url with hash = id
     *
     */
    fun getQr(hash: String, request: HttpServletRequest): ResponseEntity<QrDataOut>

}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val createQr: Boolean = false,
    val sponsor: String? = null
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val qr: String? = null,
    val properties: Map<String, Any> = emptyMap()
)

/**
 * Data returned when asked for a specific QR
 */
data class QrDataOut(
    val image: String? = null,
    val error: String? = null
)

/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class UrlShortenerControllerImpl(
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase,
    val createQrUseCase: CreateQrUseCase,
    val validateUseCase: ValidateUseCase
) : UrlShortenerController {

    @Autowired
    private val validationQueue: BlockingQueue<String>? = null

    @Autowired
    private val multiThreadValidator: Scheduler? = null

    @GetMapping("/tiny-{id:.*}")
    @Timed(description = "Time spent redirecting to the original URL")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Void> =
        redirectUseCase.redirectTo(id).let {
            logClickUseCase.logClick(id, ClickProperties(
                ip = request.remoteAddr,
                browser = request.getHeader("User-Agent")
            ))
            val h = HttpHeaders()

            //The uri is inaccessible as long as it has not been validated.
            if (validateUseCase.isValidated(id)) {
                h.location = URI.create(it.target)
                ResponseEntity<Void>(h, HttpStatus.valueOf(it.mode))
            } else {       
                ResponseEntity<Void>(h, HttpStatus.BAD_REQUEST)
            }
        }

    @PostMapping("/api/link", consumes = [ MediaType.APPLICATION_FORM_URLENCODED_VALUE ])
    @Timed(description = "Time spent creating the shortened URL")
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor,
                browser = request.getHeader("User-Agent"),
            )
        ).let {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
            h.location = url
            //Add the url to the verification queue
            validationQueue?.put(data.url)

            multiThreadValidator?.execute()

            if (data.createQr) {
                val qrUrl = createQrUseCase.create(it.hash, url.toString())
                val response = ShortUrlDataOut(
                    url = url,
                    properties = mapOf(
                        "safe" to it.properties.safe
                    ),
                    qr = qrUrl
                )

                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
            } else {
                val response = ShortUrlDataOut(
                    url = url,
                    properties = mapOf(
                        "safe" to false
                    )
                )

                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
            }
        }

    @GetMapping("/qr/{hash}")
    @Timed(description = "Time spent returning the QR image")
    override fun getQr(@PathVariable hash: String, request: HttpServletRequest): ResponseEntity<QrDataOut> {
        val h = HttpHeaders()

        val qrImage = createQrUseCase.get(hash)
        if (qrImage != "") {
            val response = QrDataOut(
                image = qrImage
            )
            return ResponseEntity<QrDataOut>(response, h, HttpStatus.OK)

        } else {
            val response = QrDataOut(
                error = "URL de destino no validada todavía"
            )
            return ResponseEntity<QrDataOut>(response, h, HttpStatus.NOT_FOUND)
        }
    }

}
