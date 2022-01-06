package es.unizar.urlshortener.core.blockingQueue

import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.net.URL
import java.util.concurrent.*

@Configuration
@EnableAsync
class QueueConfig {

    @Bean
    fun qrQueue(): BlockingQueue<String> = LinkedBlockingQueue()

    @Bean("qrExecutor")
    fun qrExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 20
        executor.maxPoolSize = 20
        executor.setQueueCapacity(1000)
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.DiscardOldestPolicy ())
        executor.setThreadNamePrefix("Qrator-")
        executor.initialize()
        return executor
    }





}