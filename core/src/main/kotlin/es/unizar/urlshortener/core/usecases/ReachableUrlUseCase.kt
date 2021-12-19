package es.unizar.urlshortener.core.usecases

import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException


interface ReachableUrlUseCase {
    fun isReachable(url: String) : Boolean
    fun isReachable2(url: String) : Boolean
}

class ReachableUrlUseCaseImpl() : ReachableUrlUseCase{

    override fun isReachable(url: String) : Boolean {

        val url = URL(url)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 3 * 1000
        return try {
            connection.connect()
            connection.responseCode == HttpStatus.OK.value()
        }catch (timeOutException : SocketTimeoutException){
            //print("Timeout")
            false
        }catch (openingConnectionError : IOException){
            //print("Unknown host")
            false
        }

    }

    override fun isReachable2(url: String): Boolean {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        try{
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return false
            }
            return true
        }catch (exception : IOException){
            return false
        }
    }


}