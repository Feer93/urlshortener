package es.unizar.urlshortener.core.blockingQueue

import es.unizar.urlshortener.core.usecases.ReachableUrlUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.stereotype.Component
import java.net.URL
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue


@Component
class Scheduler {


    @Autowired
    private val urlQueue: BlockingQueue<URL>? = null

    @Async
    fun scheduleEachUrlForDownload(reachableUrlUseCase: ReachableUrlUseCase): CompletableFuture<Boolean> {

        while (true) {
            try {
                // As the URLs being added by MultiThreadedDownloader, this process goes on..
                val urlTobeCrawled: URL? = urlQueue?.take()
                LOGGER.info("Scheduling URL : {}", urlTobeCrawled)
                if (urlTobeCrawled != null) {
                    val valor = reachableUrlUseCase.isReachable(urlTobeCrawled.toString())
                    return CompletableFuture.completedFuture(valor)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Scheduler::class.java)
    }
}