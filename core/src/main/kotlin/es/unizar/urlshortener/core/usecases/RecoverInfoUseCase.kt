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
    fun recoverTopKShortenedURL(): MutableList<Pair<String, Long>>
    fun recoverTopKRedirection():  MutableList<Pair<String, Long>>
}

/**
 * Implementation of [RecoverInfoUseCase].
 */
open class RecoverInfoUseCaseImpl(
    private val infoService: InfoRepositoryService
) : RecoverInfoUseCase {

    private final val K = 100

    @Cacheable("generalStats", key = "1")
    override fun countURL(): Long = infoService.countURL()

    @Cacheable("generalStats", key = "2")
    override fun countRedirection(): Long = infoService.countRedirection()

    @Cacheable("generalStats", key = "3")
    override fun recoverTopKShortenedURL(): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(K)

    @Cacheable("generalStats", key = "4")
    override fun recoverTopKRedirection():  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(K)


    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "1")
    open fun countURLUpdate(): Long = infoService.countURL()

    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "2")
    open fun countRedirectionUpdate(): Long = infoService.countRedirection()

    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "3")
    open fun recoverTopKShortenedURLUpdate(): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(K)

    @Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "4")
    open fun recoverTopKRedirectionUpdate():  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(K)

}
