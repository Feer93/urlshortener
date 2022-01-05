package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable

/**
 * Recovers info from the repositories to use it
 * to analyze the application or monitoring it.
 *
 */
interface RecoverInfoUseCase {
    /**
     * Calculate the number of URL shortened
     */
    fun countURL(): Long
    /**
     * Calculate the number of redirections executed
     */
    fun countRedirection(): Long
    /**
     * Calculate the top k hosts by the number of shortened URL they have
     */
    fun recoverTopKShortenedURL(): MutableList<Pair<String, Long>>
    /**
     * Calculate the top k shortened URL by usage
     */
    fun recoverTopKRedirection():  MutableList<Pair<String, Long>>

    /**
     * Update the number of URL shortened
     */
    fun countURLUpdate(): Long
    /**
     * Update the number of redirections executed
     */
    fun countRedirectionUpdate(): Long
    /**
     * Update the top k hosts by the number of shortened URL they have
     */
    fun recoverTopKShortenedURLUpdate(): MutableList<Pair<String, Long>>
    /**
     * Update the top k shortened URL by usage
     */
    fun recoverTopKRedirectionUpdate():  MutableList<Pair<String, Long>>
}

/**
 * Implementation of [RecoverInfoUseCase].
 */
open class RecoverInfoUseCaseImpl(
    private val infoService: InfoRepositoryService
) : RecoverInfoUseCase {

    private final val k = 100

    //Access the cache to get the data of the stats

    @Cacheable("generalStats", key = "1")
    override fun countURL(): Long = infoService.countURL()

    @Cacheable("generalStats", key = "2")
    override fun countRedirection(): Long = infoService.countRedirection()

    @Cacheable("generalStats", key = "3")
    override fun recoverTopKShortenedURL(): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(k)

    @Cacheable("generalStats", key = "4")
    override fun recoverTopKRedirection():  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(k)


    //Update the cache with the new data
    //Used in combination with a scheduler

    //@Async
    //@Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "1")
    override fun countURLUpdate(): Long = infoService.countURL()

    //@Async
    //@Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "2")
    override fun countRedirectionUpdate(): Long = infoService.countRedirection()

    //@Async
    //@Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "3")
    override fun recoverTopKShortenedURLUpdate(): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(k)

    //@Async
    //@Scheduled(fixedRate = 60L, timeUnit = TimeUnit.SECONDS)
    @CachePut("generalStats", key = "4")
    override fun recoverTopKRedirectionUpdate():  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(k)

}
