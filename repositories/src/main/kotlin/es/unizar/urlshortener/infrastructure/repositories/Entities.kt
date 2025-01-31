package es.unizar.urlshortener.infrastructure.repositories

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * The [ClickEntity] entity logs clicks.
 */
@Entity
@Table(name = "click")
class ClickEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long?,
    val hash: String,
    val created: OffsetDateTime,
    val ip: String?,
    val referrer: String?,
    val browser: String?,
    val platform: String?,
    val country: String?
)

/**
 * The [ShortUrlEntity] entity stores short urls.
 */
@Entity
@Table(name = "shorturl")
class ShortUrlEntity(
    @Id
    val hash: String,
    val target: String,
    val sponsor: String?,
    val created: OffsetDateTime,
    val owner: String?,
    val mode: Int,
    val reachable: Boolean,
    val safe: Boolean,
    val ip: String?,
    val country: String?,
    val validated: Boolean
)


/**
 * The [QrEntity] entity stores qr images.
 */
@Entity
@Table(name = "qr")
class QrEntity(
    @Id
    val hash: String,
    @Column(length=16777216)    //Increment space for image column
    val image: String
)