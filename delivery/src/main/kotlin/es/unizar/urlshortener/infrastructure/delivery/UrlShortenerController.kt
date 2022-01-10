package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.qrSchedule.QrCallable
import es.unizar.urlshortener.core.usecases.*
import io.micrometer.core.annotation.Timed
import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.validationQueue.ValidationScheduler
import es.unizar.urlshortener.core.usecases.*
import io.netty.handler.codec.http.HttpHeaders.newEntity
import io.swagger.annotations.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.links.Link
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.concurrent.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.concurrent.BlockingQueue
import javax.servlet.http.HttpServletRequest
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * The specification of the controller.
 */
interface UrlShortenerController {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<ErrorDataOut>

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
@Schema(name="ShortURLDataIn",description = "Data required to create a short URL.")
data class ShortUrlDataIn(
    @get:[Schema(name = "url", format = "String", required = true, example = "http//www.some-url.com")]
    val url: String,
    @get:[Schema(name = "createQr", format = "Boolean", allowableValues = ["True", "False"],required = false)]
    val createQr: Boolean = false,
    @get:[Schema(name = "sponsor", format = "String",required = false)]
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
 * Data returned after error Get Request
 */
data class ErrorDataOut(
        val error:String ?= null
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
    val validateUseCase: ValidateUseCase,
    val reachableUrlUseCase: ReachableUrlUseCase,
) : UrlShortenerController {

    private val qrExecutor = Executors.newFixedThreadPool(20) as ThreadPoolExecutor

    @Autowired
    private val validationQueue: BlockingQueue<String>? = null

    @Autowired
    private val multiThreadValidator: ValidationScheduler? = null

    @Operation(summary = "Redirects to a specific URL given a shorten URL")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "303",
                    description = "Redirect to the given URL",
                    content = [Content(mediaType = "html-page",
                            examples = [ExampleObject(summary = "Redirects the user to the given shorten URL",name = "URL validated as safe and reachable.")]
                    )]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "URL has not been validated yet",
                    content = [Content(mediaType = "application/json",
                            examples = [ExampleObject(value = "{'error': Error URL no validada todavía }",name = "The URL hasnt been procesed yet.")]
                            )]

            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "307",
                    description = "URL not safe or not reachable",
                    content = [Content(mediaType = "html-page",
                            examples = [ExampleObject(summary = "Redirects the user to an error page",name = "URL isnt safe or reachable", externalValue =  "http://localhost:8080/errorp")])],

            )

    )
    @GetMapping("/tiny-{id:.*}")
    @Timed(description = "Time spent redirecting to the original URL")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<ErrorDataOut> =
        redirectUseCase.redirectTo(id).let {
            logClickUseCase.logClick(id, ClickProperties(
                ip = request.remoteAddr,
                browser = request.getHeader("User-Agent")
            ))
            val h = HttpHeaders()

            //The uri is inaccessible as long as it has not been validated.
            if (validateUseCase.isValidated(id)) {
                if(validateUseCase.isSafeAndReachable(id)){
                    h.location = URI.create(it.target)
                    ResponseEntity<ErrorDataOut>(h, HttpStatus.valueOf(it.mode))
                }else{
                    val location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/errorp")
                            .buildAndExpand()
                            .toUri()

                    val re = Regex("/tiny-.*/")
                    h.location =  URI(location.toString().replace(re,"/"))

                    ResponseEntity<ErrorDataOut>(h, HttpStatus.TEMPORARY_REDIRECT)
                }
            } else {
                ResponseEntity<ErrorDataOut>(ErrorDataOut(error = "URL de destino no validada todavia"), h, HttpStatus.BAD_REQUEST)
            }
        }

    @Operation(summary = "Shorts an URL and creates a QR if specified")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "Short URL returned and accepted for processing. QR returned if specified",
                    content = [Content(mediaType = "application/json",
                            schema = Schema(implementation = ShortUrlDataOut::class ))]

            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "URL could not be processed",
                    content = [Content(mediaType = "application/json"
                            )]

            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = [Content()]
            )

    )
    @PostMapping("/api/link", consumes = [ MediaType.APPLICATION_FORM_URLENCODED_VALUE ])
    @Timed(description = "Time spent creating the shortened URL")
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =

        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor,
                browser = request.getHeader("User-Agent")
            )
        ).let {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
            h.location = url

            //Add the url to the verification queue
            validationQueue?.put(data.url)

            if (data.createQr) {
                val futureQr = qrExecutor.submit(QrCallable(createQrUseCase, data.url, it.hash))
                val response = ShortUrlDataOut(
                    url = url,
                    qr = futureQr.get()
                )

                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.ACCEPTED)
            } else {
                val response = ShortUrlDataOut(
                    url = url
                )
                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.ACCEPTED)
            }
        }
        
    @GetMapping("/qr/{hash}")
    @Timed(description = "Time spent returning the QR image")
    override fun getQr(@PathVariable hash: String, request: HttpServletRequest): ResponseEntity<QrDataOut> {
        val h = HttpHeaders()

        val qrImage = createQrUseCase.get(hash)

        if (qrImage != null) {
            val response = QrDataOut(
                image = qrImage
            )
            return ResponseEntity<QrDataOut>(response, h, HttpStatus.OK)

        } else {
            val response = QrDataOut(
                error = "La imagen QR no existe"
            )
            return ResponseEntity<QrDataOut>(response, h, HttpStatus.NOT_FOUND)
        }
    }
}
