package es.unizar.urlshortener.core

import java.time.OffsetDateTime
import java.util.*

/**
 * A [Click] captures a request of redirection of a [ShortUrl] identified by its [hash].
 */
data class Click(
    val hash: String,
    val properties: ClickProperties = ClickProperties(),
    val created: OffsetDateTime = OffsetDateTime.now()
)

/**
 * A [ShortUrl] is the mapping between a remote url identified by [redirection] and a local short url identified by [hash].
 */
data class ShortUrl(
    val hash: String,
    val redirection: Redirection,
    val created: OffsetDateTime = OffsetDateTime.now(),
    val properties: ShortUrlProperties = ShortUrlProperties()
)

/**
 * A [Redirection] specifies the [target] and the [status code][mode] of a redirection.
 * By default, the [status code][mode] is 307 TEMPORARY REDIRECT.
 */
data class Redirection(
    val target: String,
    val mode: Int = 307
)

/**
 * A [ShortUrlProperties] is the bag of properties that a [ShortUrl] may have.
 */
data class ShortUrlProperties(
        val ip: String? = null,
        val sponsor: String? = null,
        var safe:Boolean = false,
        var validated: Boolean = false,
        val owner: String? = null,
        var reachable: Boolean = false,
        val country: String? = null,
        val browser: String? = null,
        val created: OffsetDateTime? = null,
)

/**
 * A [ClickProperties] is the bag of properties that a [Click] may have.
 */
data class ClickProperties(
    val ip: String? = null,
    val referrer: String? = null,
    val browser: String? = null,
    val platform: String? = null,
    val country: String? = null,
    val created: OffsetDateTime? = null
)

/**
 * A [QrImage] is the qr associated to the [ShortUrl] with the same hash.
 */
data class QrImage(
    val hash: String,
    val image: String
)