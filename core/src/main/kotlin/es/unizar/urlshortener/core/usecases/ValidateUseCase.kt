package es.unizar.urlshortener.core.usecases

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import es.unizar.urlshortener.core.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import java.net.URI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.scheduling.annotation.Async
import org.springframework.web.client.RestTemplate;

enum class ValidationResponse {
    VALID,
    UNSAFE
}

/* Given a url returns if it is valid: safe. */
interface ValidateUseCase {
    fun validate(url: String): ValidationResponse
    fun isSafe(url: String): ValidationResponse
    fun isValidated(hash: String): Boolean
}

const val API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyDiXbmdOrpATVTSu5FqGCb98jMmE6cJ-c8"

/* Implementation of [ValidateUseCase]. */
open class ValidateUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val meterRegistry: MeterRegistry
) : ValidateUseCase {

    @Autowired lateinit var restTemplate: RestTemplate

    private var validCounter: Counter = Counter.builder("validate.url").
        tag("type", "validURL").
        description("Number of URLs validated").
        register(meterRegistry)

    private var invalidCounter: Counter = Counter.builder("validate.url").
        tag("type", "invalidURL").
        description("Number of URLs rejected").
        register(meterRegistry)

    @Async
    open fun updateValid(){
        validCounter.increment()
    }

    @Async
    open fun updateInvalid(){
        invalidCounter.increment()
    }

    override fun validate(url: String): ValidationResponse {
        return isSafe(url) /*&& isReachable(url)*/
    }

    override fun isSafe(url: String): ValidationResponse {
        val uri = URI(API_URL)

        //Create request body
        val body = ThreatMatchesRequest(
            ThreatInfo(
                listOf(ThreatType.MALWARE, ThreatType.SOCIAL_ENGINEERING),
                listOf(PlatformType.ALL_PLATFORMS),
                listOf(ThreatEntryType.URL),
                listOf(
                    ThreatEntry(url, ThreatEntryRequestType.URL)
                )
            )
        )

        // Convert body to json
        val requestBody = jacksonObjectMapper().writeValueAsString(body)
        val request = HttpEntity(requestBody)

        //Request to google safe browsing
        val response = restTemplate.postForObject(uri, request,ThreatMatchesResponse::class.java)

        //If the resposponse is empty, the url is secure
        if (!response?.matches.isNullOrEmpty()) {
            updateInvalid()
            return ValidationResponse.UNSAFE
        }
        updateValid()
        return ValidationResponse.VALID 
    }

    /* Returns if a [ShortUrl] is masked as validated. */
    override fun isValidated(hash: String) : Boolean {
        var shortUrl: ShortUrl = shortUrlRepository.findByKey(hash)!!

        return shortUrl.properties.validated
    }
}
