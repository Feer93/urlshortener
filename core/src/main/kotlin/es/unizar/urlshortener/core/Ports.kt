package es.unizar.urlshortener.core

import java.util.concurrent.BlockingQueue

/**
 * [ClickRepositoryService] is the port to the repository that provides persistence to [Clicks][Click].
 */
interface ClickRepositoryService {
    fun save(cl: Click): Click
}

/**
 * [ShortUrlRepositoryService] is the port to the repository that provides management to [ShortUrl][ShortUrl].
 */
interface ShortUrlRepositoryService {
    fun findByKey(id: String): ShortUrl?
    fun findByUrl(url: String): ShortUrl?
    fun save(su: ShortUrl): ShortUrl
}

/**
 * [QrRepositoryService] is the port to the repository that provides management to a QR image.
 */
interface QrRepositoryService {
    fun findByKey(id: String): QrImage?
    fun save(qrImage: QrImage): QrImage
}

/**
 * [ValidatorService] is the port to the service that validates if an url can be shortened.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface ValidatorService {
    fun isValid(url: String): Boolean
}


/**
 * [InfoRepositoryService] is the port to the service that recovers info from the repositories.
 *
 */
interface InfoRepositoryService {
    fun countURL(): Long
    fun countRedirection(): Long
    fun recoverTopKShortenedURL(k: Int): MutableList<Pair<String, Long>>
    fun recoverTopKRedirection(k: Int):  MutableList<Pair<String, Long>>

}

/**
 * [HashService] is the port to the service that creates a hash from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface HashService {
    fun hasUrl(url: String): String
}