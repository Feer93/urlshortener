package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit

/**
 * Recovers info from the repositories to use it
 * to analyze the application or monitoring it.
 *
 */
interface RecoverInfoUseCase {
    fun countURL(): Long
    fun countRedirection(): Long
    fun recoverTopKShortenedURL(k: Int): MutableList<Pair<String, Long>>
    fun recoverTopKRedirection(k: Int):  MutableList<Pair<String, Long>>
}

/**
 * Implementation of [RecoverInfoUseCase].
 */
open class RecoverInfoUseCaseImpl(
    private val infoService: InfoRepositoryService
) : RecoverInfoUseCase {

    @Cacheable("countURL")
    override fun countURL(): Long = infoService.countURL()

    @Cacheable("countRedirection")
    override fun countRedirection(): Long = infoService.countRedirection()

    @Cacheable("TopKShortenedURL", key = "100")
    override fun recoverTopKShortenedURL(k: Int): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(k)

    @Cacheable("TopKRedirection", key = "100")
    override fun recoverTopKRedirection(k: Int):  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(k)


    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("countURL")
    open fun countURLUpdate(): Long = infoService.countURL()

    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("countRedirection")
    open fun countRedirectionUpdate(): Long = infoService.countRedirection()

    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("TopKShortenedURL", key = "100")
    open fun recoverTopKShortenedURLUpdate(): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(100)

    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("TopKRedirection", key = "100")
    open fun recoverTopKRedirectionUpdate():  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(100)

}
