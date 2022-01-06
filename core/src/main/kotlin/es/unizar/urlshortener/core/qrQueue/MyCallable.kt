package es.unizar.urlshortener.core.qrQueue

import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import es.unizar.urlshortener.core.usecases.CreateQrUseCaseImpl
import java.util.*
import java.util.concurrent.*


class MyCallable : Callable<String> {

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
        System.out.println(Thread.currentThread().name)
        Thread.sleep(5000)
        //return the thread name executing this callable task

        if (qr != null) {
            return qr.get()
        }

        return ""
    }




}