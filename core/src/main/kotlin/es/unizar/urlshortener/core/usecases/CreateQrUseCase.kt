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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
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
    fun create(url: String?, hash: String?, qrUrl : String?): CompletableFuture<String>

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



    /**
     * Counter to count the number of times a qr is used
     */
    private var qrCounter: Counter = Counter.builder("user.action").
        tag("type", "qrUsed").
        register(meterRegistry)

    /**
     * Increment in 1 the value of the counter
     */
    //@Async
    open fun updateQrCounter(){
        qrCounter.increment()
    }

    /**
     * Creates the QR associated to 'url' as a string Base64 encoded image
     * and then returns the URL to get the QR.
    */
    @Async
    override fun create(url: String?, hash : String?, qrUrl : String?): CompletableFuture<String> {

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


        return CompletableFuture.completedFuture(qrUrl)
    }

    /**
     * Return image associated to 'hash' if it exists.
     */
    override fun get(hash: String): String? {

        val qrImage = qrRepositoryService.findByKey(hash)
        if(qrImage != null){
            updateQrCounter()
        }
        return qrImage?.image
    }

}
