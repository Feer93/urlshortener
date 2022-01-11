package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.RecoverInfoUseCase
import io.micrometer.core.annotation.Timed
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 * The specification of the Stats controller.
 */
interface StatsController {

    /**
     * TODO
     */
    fun statsSpecific(id: String, request: HttpServletRequest): ResponseEntity<StatsOut>

    /**
     *
     * Handle a general stats request by returning the data of the general stats
     *
     * **Note**: Delivery of use case [RecoverInfoUseCase].
     */
    fun statsGeneral(hash: String, request: HttpServletRequest): ResponseEntity<GeneralStatsOut>

}

/**
 * Data that the controller returns
 */
data class StatsOut(
    val statID: Long? = null,
    val statCount: Long? = null,
    val statList: MutableList<Pair<String, Long>>? = null,
    val error: String? = null
)

/**
 * Data that the controller returns when asked for general stats
 */
data class GeneralStatsOut(
    val description: String? = null,
    val totalShortenedURL: Long? = null,
    val totalClicks: Long? = null,
    val top100clickedShortenedURL: MutableList<Pair<String, Long>> = mutableListOf(),
    val top100hostsWithShortenedURL: MutableList<Pair<String, Long>> = mutableListOf()
)


/**
 * The implementation of the Stats controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class StatsControllerImpl(
    val recoverInfoUseCase: RecoverInfoUseCase
) : StatsController {

    /**
     * TODO: Handle a specific stats request
     */
    @GetMapping("/stat-{id:[0-9]+}")
    @Timed(description = "Time spent calculating specific stats")
    override fun statsSpecific(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<StatsOut> {
        val h = HttpHeaders()
        val idF = id.toLong()

        val response : StatsOut

        when(idF) {
            1L -> response = StatsOut(
                    statID = idF,
                    statCount = recoverInfoUseCase.countURL()
                )
            2L -> response = StatsOut(
                    statID = idF,
                    statCount = recoverInfoUseCase.countRedirection()
                )
            3L -> response = StatsOut(
                    statID = idF,
                    statList = recoverInfoUseCase.recoverTopKRedirection()
                )
            4L -> response = StatsOut(
                    statID = idF,
                    statList = recoverInfoUseCase.recoverTopKShortenedURL()
                )
            else -> {
                response = StatsOut(
                    error = "Error: $idF invalid stat id"
                )
            }
        }

        return ResponseEntity<StatsOut>(response, h, HttpStatus.OK)
    }


    @GetMapping("/{hash:.+}.json")
    @Timed(description = "Time spent calculating general stats")
    override fun statsGeneral(@PathVariable hash: String, request: HttpServletRequest): ResponseEntity<GeneralStatsOut> {
        val h = HttpHeaders()
        //Recover stats from the cache
        val response = GeneralStatsOut(
            description = hash,
            totalShortenedURL = recoverInfoUseCase.countURL(),
            totalClicks = recoverInfoUseCase.countRedirection(),
            top100clickedShortenedURL = recoverInfoUseCase.recoverTopKRedirection(),
            top100hostsWithShortenedURL = recoverInfoUseCase.recoverTopKShortenedURL()
        )
        return ResponseEntity<GeneralStatsOut>(response, h, HttpStatus.OK)
    }
}
