package es.unizar.urlshortener.core.blockingQueue

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue


@Component
class Scheduler(
    val shortUrlRepository: ShortUrlRepositoryService,
    val validateUseCase: ValidateUseCase
) {

    @Autowired
    private val validationQueue: BlockingQueue<String>? = null

    // ? TODO: Hay alguna manera de que empiece al principio sin tenerla que llamar cada vez que llega una url ????
    @Async("validationExecutor")
    fun execute(/*validateUseCase: ValidateUseCase*/) {
        val url: String = validationQueue!!.take()

        val validationResponse = validateUseCase.validate(url)

        if (validationResponse == ValidationResponse.VALID) {
            println("Es segura")
        } else if (validationResponse == ValidationResponse.UNSAFE){
            println("No es segura")
        }

        //Takes the [ShortUrl] from the repository and marks it as validated
        var shortUrl: ShortUrl = shortUrlRepository.findByUrl(url)!!

        shortUrl.properties.validated = true

        shortUrlRepository.save(shortUrl)
    }
}