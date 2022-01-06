package es.unizar.urlshortener.core.usecases

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.qrSchedule.QrCallable
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import javax.swing.Spring.height


/**
 * [CreateQrUseCase] is the port to the service that creates a QR from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface CreateQrUseCase {
    fun create(url: String?, hash: String?): CompletableFuture<String>

    fun get(hash: String) : String?
}

/**
 * Implementation of the port [CreateQrUseCase].
 */
@Component
open class CreateQrUseCaseImpl  (
    private val qrRepositoryService: QrRepositoryService,
    private val meterRegistry: MeterRegistry
    ) : CreateQrUseCase {


    private val hostName = "http://localhost:8080/"

    private var qrCounter: Counter = Counter.builder("user.action").
        tag("type", "qrUsed").
        description("Number of QRs used").
        register(meterRegistry)

    @Async
    open fun updateQrCounter(){
        qrCounter.increment()
    }

    //Return String with ByteArray QR image data Base64 encoded
    @Async
    override fun create(url: String?, hash : String?): CompletableFuture<String> {

        val imageString = get(hash.toString())

        if (imageString == null) {

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 100, 100)

            val pngOutputStream = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)

            val imageByteArray = pngOutputStream.toByteArray()
            val imageString = Base64.getEncoder().encodeToString(imageByteArray)


            val qrImage = QrImage(
                hash = hash.toString(),
                image = imageString
            )

            qrRepositoryService.save(qrImage)

        }

        return CompletableFuture.completedFuture("$hostName" + "qr/" + "$hash")
    }

    override fun get(hash: String): String? {

        val qrImage = qrRepositoryService.findByKey(hash)
        updateQrCounter()
        return qrImage?.image
    }

    companion object {
        private val Logger = LoggerFactory.getLogger(CreateQrUseCaseImpl::class.java)
    }

}
