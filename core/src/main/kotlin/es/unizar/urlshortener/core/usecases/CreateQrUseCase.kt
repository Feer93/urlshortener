package es.unizar.urlshortener.core.usecases

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import es.unizar.urlshortener.core.*
import java.io.ByteArrayOutputStream
import java.util.*
import javax.swing.Spring.height


/**
 * [CreateQrUseCase] is the port to the service that creates a QR from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface CreateQrUseCase {
    fun create(url: String): String
}

/**
 * Implementation of the port [CreateQrUseCase].
 */
class CreateQrUseCaseImpl : CreateQrUseCase {
    //Return String with ByteArray QR image data Base64 encoded
    override fun create(url: String): String {

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 100, 100)

        val pngOutputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)

        val imageByteArray = pngOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(imageByteArray)
    }

}
