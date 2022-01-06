package es.unizar.urlshortener.core.blockingQueue

import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor


@Component
class QrScheduler {


    @Autowired
    private val qrQueue: BlockingQueue<String>? = null

    @Async
    fun scheduleEachUrlForDownload(createQrUseCase: CreateQrUseCase): CompletableFuture<String>? {

        while (true) {

            // As the URLs being added by MultiThreadedDownloader, this process goes on..
            val urlTobeCrawled: String? = qrQueue?.take()

            if(urlTobeCrawled != null) {
                val valor = createQrUseCase.create(urlTobeCrawled.toString(), urlTobeCrawled.toString())
                return CompletableFuture.completedFuture(valor.toString())
            }
        }

    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(QrScheduler::class.java)
    }
}


