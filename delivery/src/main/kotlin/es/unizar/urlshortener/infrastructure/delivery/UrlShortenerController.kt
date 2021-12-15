package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import io.micrometer.core.annotation.Timed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.web.bind.annotation.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.nio.channels.Channel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest


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

    fun testing(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
        var url: URI? = null,
        val properties: Map<String, Any> = emptyMap()
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
    val reachableUrlUseCase: ReachableUrlUseCase,
) : UrlShortenerController {


    private val LOGGER: Logger = LoggerFactory.getLogger(UrlShortenerControllerImpl::class.java)



    @Autowired
    private val urlQueue: BlockingQueue<URL>? = null

    @Autowired
    private val multiThreadDownloader: es.unizar.urlshortener.core.blockingQueue.Scheduler? = null

    @Async
    fun startScrapping(URLs: URL) : CompletableFuture<Boolean?> {
        LOGGER.info("Starting crawler....")
        // Añadimos las URLs a la blockingqueue
        urlQueue?.put(URLs)
        // Empezamos a schedulear las URLs
        val futureReturn: Future<Boolean>? = multiThreadDownloader?.scheduleEachUrlForDownload(reachableUrlUseCase)
        val start = System.currentTimeMillis();
        val resultado= futureReturn?.get()
        val end = System.currentTimeMillis();
        LOGGER.info("Resultado listo en " + (end-start))
        LOGGER.info("Resultado listo " + resultado)
        return  CompletableFuture.completedFuture(resultado);

    }

    @GetMapping("/tiny-{id:.*}")
    @Timed(description = "Time spent redirecting to the original URL")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Void> =
        redirectUseCase.redirectTo(id).let {
            logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
            val h = HttpHeaders()
            h.location = URI.create(it.target)
            ResponseEntity<Void>(h, HttpStatus.valueOf(it.mode))
        }

    @PostMapping("/api/link", consumes = [ MediaType.APPLICATION_FORM_URLENCODED_VALUE ])
    @Timed(description = "Time spent creating the shortened URL")
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
       try {
           createShortUrlUseCase.create(
                   url = data.url,
                   data = ShortUrlProperties(
                           ip = request.remoteAddr,
                           sponsor = data.sponsor
                   )
           ).let {
               val h = HttpHeaders()
               val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
               h.location = url


               if (!reachableUrlUseCase.isReachable(data.url)) {
                   val response = ShortUrlDataOut(
                           url = null,
                           properties = mapOf(
                                   "Error" to "URI de destino no validada todavia"
                           )
                   )
                   ResponseEntity<ShortUrlDataOut>(response, HttpStatus.BAD_REQUEST)
               } else{
               val response = ShortUrlDataOut(
                       url = url,
                       properties = mapOf(
                               "safe" to it.properties.safe
                       )
               )
               ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
                }
           }
       }catch (invalidURL: InvalidUrlException){
           val response = ShortUrlDataOut(
                   url = null,
                   properties = mapOf(
                           "Error" to "Uri invalida"
                   )
           )
          ResponseEntity<ShortUrlDataOut>(response,HttpStatus.BAD_REQUEST)
       }

    @PostMapping("/api/testing", consumes = [ MediaType.APPLICATION_FORM_URLENCODED_VALUE ])
    override fun testing(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
     try {
         System.out.println(data.url)
         createShortUrlUseCase.create(
                 url = data.url,
                 data = ShortUrlProperties(
                         ip = request.remoteAddr,
                         sponsor = data.sponsor
                 )
         ).let {

             val reachableURL: CompletableFuture<Boolean?> = startScrapping(URL(data.url))


             CompletableFuture.allOf(
                     reachableURL
             ).get()

             if (reachableURL.get() == true) {
                 val response = ShortUrlDataOut(
                         url = null,
                         properties = mutableMapOf(
                                 "Reachable" to "Respuesta"
                         )
                 )
                 response.url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
                 ResponseEntity<ShortUrlDataOut>(response, HttpHeaders(), HttpStatus.CREATED)
             }else {
                 val response = ShortUrlDataOut(
                         url = null,
                         properties = mutableMapOf(
                                 "Non-Reachable" to "Respuesta"
                         )
                 )
                 ResponseEntity<ShortUrlDataOut>(response, HttpHeaders(), HttpStatus.CREATED)
             }
         }

     }catch (invalidURL: InvalidUrlException){

         val response = ShortUrlDataOut(
                 url = null,
                 properties = mapOf(
                         "Error" to "Uri invalida"
                 )
         )
         ResponseEntity<ShortUrlDataOut>(response, HttpHeaders(), HttpStatus.CREATED)
     }

}
