package es.unizar.urlshortener.core.usecases

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection


interface ReachableUrlUseCase {
    fun isReachable(url: String) : Boolean
    fun isReachable2(url: String) : Boolean
}

class ReachableUrlUseCaseImpl(private val meterRegistry: MeterRegistry) : ReachableUrlUseCase{

    /**
     * Counter to count the number of reachable URL given to shorten
     */
    private var reachableCounter: Counter = Counter.builder("reach.url").
    tag("type", "reachableURL").
    register(meterRegistry)

    /**
     * Counter to count the number of unreachable URL given to shorten
     */
    private var unreachableCounter: Counter = Counter.builder("reach.url").
    tag("type", "unreachableURL").
    register(meterRegistry)


    override fun isReachable(url: String) : Boolean {

        val url = URL(url)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 3 * 1000
        return try {
            connection.connect()
            if(connection.responseCode == HttpStatus.OK.value()){
                reachableCounter.increment()
                true
            } else {
                unreachableCounter.increment()
                false
            }
        }catch (timeOutException : SocketTimeoutException){
            //print("Timeout")
            unreachableCounter.increment()
            false
        }catch (openingConnectionError : IOException){
            //print("Unknown host")
            unreachableCounter.increment()
            false
        }

    }

    override fun isReachable2(url: String): Boolean {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        try{
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                //Increment in 1 the value of the counter that counts unreachable URLs
                unreachableCounter.increment()
                return false
            }
            //Increment in 1 the value of the counter that counts reachable URLs
            reachableCounter.increment()
            return true
        }catch (exception : IOException){
            //Increment in 1 the value of the counter that counts unreachable URLs
            unreachableCounter.increment()
            return false
        }
    }


}