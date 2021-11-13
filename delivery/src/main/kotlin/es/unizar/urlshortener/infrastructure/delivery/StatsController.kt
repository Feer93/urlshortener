package es.unizar.urlshortener.infrastructure.delivery

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.maxmind.geoip2.DatabaseReader
import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.RecoverInfoUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
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

/**
 * The specification of the controller.
 */
interface StatsController {


    fun statsSpecific(id: String, request: HttpServletRequest): ResponseEntity<StatsOut>


    fun statsGeneral(hash: String, request: HttpServletRequest): ResponseEntity<StatsOut>

}


data class StatsOut(
    val stat: String? = null
)


/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class StatsControllerImpl(
) : StatsController {

    @GetMapping("/shortURL-{id:.*}")
    @Timed(description = "Time spent calculating specific stats")
    override fun statsSpecific(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<StatsOut> {
        val h = HttpHeaders()
        val response = StatsOut(id)
        return ResponseEntity<StatsOut>(response, h, HttpStatus.OK)
    }

    @GetMapping("/{hash:.*}.json")
    @Timed(description = "Time spent calculating general stats")
    override fun statsGeneral(@PathVariable hash: String, request: HttpServletRequest): ResponseEntity<StatsOut> {
        val h = HttpHeaders()
        val response = StatsOut(hash)
        return ResponseEntity<StatsOut>(response, h, HttpStatus.OK)
    }
}
