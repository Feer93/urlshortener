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


/* Given a url returns if it is valid: safe. */
interface ValidateUseCase {
    fun isSafe(url: String): Boolean
    fun isValidated(hash: String): Boolean
    fun isSafeAndReachable(hash: String): Boolean
}

const val API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyDiXbmdOrpATVTSu5FqGCb98jMmE6cJ-c8"

/* Implementation of [ValidateUseCase]. */
open class ValidateUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val meterRegistry: MeterRegistry
) : ValidateUseCase {

    @Autowired lateinit var restTemplate: RestTemplate

    /**
     * Counter to count the number of valid URL given to shorten
     */
    private var validCounter: Counter = Counter.builder("validate.url").
        tag("type", "validURL").
        register(meterRegistry)

    /**
     * Counter to count the number of invalid URL given to shorten
     */
    private var invalidCounter: Counter = Counter.builder("validate.url").
        tag("type", "invalidURL").
        register(meterRegistry)


    /**
     *  Increment in 1 the value of the counter of valid URL
     */
    open fun updateValid(){
        validCounter.increment()
    }

    /**
     *  Increment in 1 the value of the counter of invalid URL
     */
    open fun updateInvalid(){
        invalidCounter.increment()
    }

    /*Check if an url is secure with Google Safe Browsing */
    override fun isSafe(url: String): Boolean {
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
        if (response?.matches.isNullOrEmpty()) {
            updateValid()
            return true
        }
        updateInvalid()
        return false
    }

    /* Returns if a [ShortUrl] is masked as validated. */
    override fun isValidated(hash: String) : Boolean {
        val shortUrl: ShortUrl = shortUrlRepository.findByKey(hash)!!
        return shortUrl.properties.validated
    }

    override fun isSafeAndReachable(hash: String): Boolean {
        val shortUrl: ShortUrl = shortUrlRepository.findByKey(hash)!!
        return shortUrl.properties.reachable && shortUrl.properties.safe
    }

}
