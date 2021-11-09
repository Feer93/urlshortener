package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.SecurityService
import es.unizar.urlshortener.core.ValidatorService
import java.nio.charset.StandardCharsets
import org.apache.commons.validator.routines.UrlValidator

/** Implementation of the port [ValidatorService]. */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url)

    companion object {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/** Implementation of the port [HashService]. */
@Suppress("UnstableApiUsage")
class HashServiceImpl : HashService {
    override fun hasUrl(url: String) =
            Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString()
}

/** Implementation of the port [SecurityService]. */
class SecurityServiceImpl : SecurityService {
    override fun isSafety(url: String): Boolean {
        var safety = true

        val api = """https://safebrowsing.googleapis.com/v4/threatMatches:find\?key\=AIzaSyDiXbmdOrpATVTSu5FqGCb98jMmE6cJ-c8"""

        val body = """{
                "threatInfo": {
                    "threatTypes":      ["MALWARE", "SOCIAL_ENGINEERING"],
                    "platformTypes":    ["WINDOWS", "LINUX],
                    "threatEntryTypes": ["URL"],
                    "threatEntries":    [{"url": "$url"},]
                } 
            }"""

        //TODO: POST and check if the response es "{}" or not

        return safety
    }
}
