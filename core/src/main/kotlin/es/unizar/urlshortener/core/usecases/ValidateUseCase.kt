package es.unizar.urlshortener.core.usecases

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import es.unizar.urlshortener.core.*
import java.net.URI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.client.RestTemplate;

enum class ValidationResponse {
    VALID,
    UNSAFE
}

/* Given a url returns if it is valid: safe. */
interface ValidateUseCase {
    fun validate(url: String): ValidationResponse
    fun isSafe(url: String): ValidationResponse
}

const val API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyDiXbmdOrpATVTSu5FqGCb98jMmE6cJ-c8"

/* Implementation of [ValidateUseCase]. */
class ValidateUseCaseImpl() : ValidateUseCase {

    @Autowired lateinit var restTemplate: RestTemplate

    override fun validate(url: String): ValidationResponse {
        return isSafe(url)
    }

    override fun isSafe(url: String): ValidationResponse {
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
        val requestBody = jacksonObjectMapper().writeValueAsString(body)

        val response = 
            restTemplate.postForObject(URI(API_URL), HttpEntity(requestBody), ThreatMatchesResponse::class.java)

        if (response?.matches.isNullOrEmpty()) {
            return ValidationResponse.VALID 
        }
        
        return ValidationResponse.UNSAFE
    }
}
