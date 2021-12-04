package es.unizar.urlshortener.core.blockingQueue

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue


@Component
class Scheduler(
    private val shortUrlRepository: ShortUrlRepositoryService
) {

    @Autowired
    private val validationQueue: BlockingQueue<String>? = null

    // ? Hay alguna manera de que empiece al principio sin tenerla que llamar??
    @Async("validationExecutor")
    fun execute(/*validateUseCase: ValidateUseCase*/) {
        val url: String? = validationQueue?.take()
        println(url)
    }
}