package es.unizar.urlshortener.core.usecases

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import es.unizar.urlshortener.core.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection


internal class ReachableUrlUseCaseImplTest {

    object ObjBeingMocked {
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

    }

    @Test
    fun `URL is reachable`() {
        assertTrue(ObjBeingMocked.isReachable("https://www.google.com/"))
    }


}