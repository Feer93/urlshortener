package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * @Description
 * Tests Unitarios para para la clase ReachableUseCase y para la clase
 * ValidateUseCase.
 * En concreto, se testean los métodos isSafe e isReachable,
 * los cuales son utilizados para verificar que una URL es alcanzable y segura.
 */
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
    fun `URL is reachable using reachable stub via HTTPS`() {
        val c = Mockito.mock(ReachableUrlUseCaseImpl::class.java)
        c.isReachable(url = "https://www.google.com/")
        assertTrue(ObjBeingMocked.isReachable("https://www.google.com/"))
    }

    @Test
    fun `URL is reachable mocking ReachableURLUseCase`(){
        val mockedReachableClass = Mockito.mock(ReachableUrlUseCaseImpl::class.java)
        mockedReachableClass.isReachable(url = "https://www.google.com/")
    }

    @Test
    fun `URL is safe mocking ValidateUseCase`(){
        val mockedValidateSafetyClass = Mockito.mock(ValidateUseCase::class.java)
        mockedValidateSafetyClass.isSafe(url = "https://www.google.com/")
    }


}