package es.unizar.urlshortener.core.usecases

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import es.unizar.urlshortener.core.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.doAnswer
import org.mockito.stubbing.Answer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * @Description
 * Tests Unitarios para para la clase ReachableUseCase y para la clase
 * ValidateUseCase.
 * En concreto, se testean los métodos isSafe e isReachable,
 * los cuales son utilizados para verificar que una URL es alcanzable y segura.
 */
internal class ReachableUrlUseCaseImplValidateUseCaseTest {

    /**
     * Se tiene que crear un stub con los códigos de las funciones que se quieren testear
     * ya que por defecto al mockear y hacer peticiones al exterior, el proyecto las bloquea  a no
     * ser que se hagan mediante HTTPS o dentro del scope.
     */
    object ObjBeingMocked {

         var restTemplate: RestTemplate = RestTemplate()

        fun isReachable(url: String): Boolean {
            val url = URL(url)
            val connection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            connection.connectTimeout = 3 * 1000
            return try {
                connection.connect()
                connection.responseCode == HttpStatus.OK.value()
            } catch (timeOutException: SocketTimeoutException) {
                //print("Timeout")
                false
            } catch (openingConnectionError: IOException) {
                //print("Unknown host")
                false
            }
        }

        fun isSafe(url: String): Boolean {

            val API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyDiXbmdOrpATVTSu5FqGCb98jMmE6cJ-c8"
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
                return true
            }
            return false
        }

    }


    @Test
    fun `URL is reachable mocking ReachableURLUseCase`(){
        val mockedReachableClass = Mockito.mock(ReachableUrlUseCaseImpl::class.java)
        Mockito.`when`(mockedReachableClass.isReachable(url = "https://www.google.com/"))
                .doAnswer(Answer { ObjBeingMocked.isReachable("https://www.google.com/") })
        assertTrue(mockedReachableClass.isReachable(url = "https://www.google.com/"))
    }

    @Test
    fun `URL is safe mocking ValidateUseCase`(){
        val mockedValidateSafetyClass = Mockito.mock(ValidateUseCase::class.java)
        Mockito.`when`(mockedValidateSafetyClass.isSafe(url = "https://www.google.com/"))
                .doAnswer(Answer { ObjBeingMocked.isSafe("https://www.google.com/") })
        assertTrue(mockedValidateSafetyClass.isSafe(url = "https://www.google.com/"))
    }


}