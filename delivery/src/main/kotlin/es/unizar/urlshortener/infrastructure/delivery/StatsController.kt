package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.RecoverInfoUseCase
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
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
     * Handle a specific stats request by returning the data of the specific stat
     *
     * **Note**: Delivery of use case [RecoverInfoUseCase].
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
@Schema(name="GeneralStatsOut",description = "General Stats from the application")
data class GeneralStatsOut(
    @get:[Schema(name = "description", type = "String",description = "Name given")]
    val description: String? = null,
    @get:[Schema(name = "totalShortenedURL", type = "integer",format = "int64",description = "Total number of shortened URL")]
    val totalShortenedURL: Long? = null,
    @get:[Schema(name = "totalClicks", type = "integer",format = "int64",description = "Total number of redirections made using a shortened URL")]
    val totalClicks: Long? = null,
    @get:[Schema(name = "top100clickedShortenedURL", type = "array",oneOf =[String::class,Long::class],description = "Total number of redirections made using a shortened URL")]
    val top100clickedShortenedURL: MutableList<Pair<String, Long>> = mutableListOf(),
    @get:[Schema(name = "top100hostsWithShortenedURL", type = "array",oneOf =[String::class,Long::class],description = "Top 100 hosts by the number of shortened URL they have")]
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
                return ResponseEntity<StatsOut>(response, h, HttpStatus.NOT_FOUND)
            }
        }

        return ResponseEntity<StatsOut>(response, h, HttpStatus.OK)
    }

    @Operation(summary = "Obtain general stats.", description = "Calculate general stats of the application",operationId = "statsGeneral")
    @Parameters(value = [Parameter(name = "hash",`in` = ParameterIn.PATH,description = "Any name you want to enter")])
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = [Content(mediaType = "application/json",
                            schema = Schema(implementation = GeneralStatsOut::class, ))]

            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid name supplied",
            )
    )
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
