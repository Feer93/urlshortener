package es.unizar.urlshortener.core.qrSchedule

import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import es.unizar.urlshortener.core.usecases.CreateQrUseCaseImpl
import org.slf4j.LoggerFactory
import java.util.concurrent.*


class QrCallable : Callable<String> {

    private var createQrUseCase : CreateQrUseCase? = null
    private var url : String? = null
    private var hash : String? = null

    constructor(task : CreateQrUseCase, url_ : String, hash_ : String) {
        createQrUseCase = task
        url=url_
        hash=hash_

    }

    @Throws(Exception::class)
    override fun call(): String {

        val qr = createQrUseCase?.create(url, hash)
        Logger.info(Thread.currentThread().id.toString())

        if (qr != null) {
            return qr.get()
        }

        return ""
    }

    companion object {
        private val Logger = LoggerFactory.getLogger(QrCallable::class.java)
    }



}