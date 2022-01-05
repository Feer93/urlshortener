package es.unizar.urlshortener.core.blockingQueue

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.net.URL
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
@EnableScheduling
class QueueConfig {

    @Bean
    fun validationQueue(): BlockingQueue<String> = LinkedBlockingQueue()

    @Bean("validationExecutor")
    fun validationExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 20
        executor.maxPoolSize = 20
        executor.setQueueCapacity(1000)
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.DiscardOldestPolicy ())
        executor.setThreadNamePrefix("ValidatorExec-")
        executor.initialize()
        return executor
    }
}