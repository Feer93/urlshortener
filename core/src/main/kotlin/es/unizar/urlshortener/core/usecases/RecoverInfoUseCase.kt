package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.util.Date

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
class RecoverInfoUseCaseImpl(
    private val infoService: InfoRepositoryService
) : RecoverInfoUseCase {

    override fun countURL(): Long = infoService.countURL()
    override fun countRedirection(): Long = infoService.countRedirection()
    override fun recoverTopKShortenedURL(k: Int): MutableList<Pair<String, Long>> =
        infoService.recoverTopKShortenedURL(k)
    override fun recoverTopKRedirection(k: Int):  MutableList<Pair<String, Long>> =
        infoService.recoverTopKRedirection(k)
}
