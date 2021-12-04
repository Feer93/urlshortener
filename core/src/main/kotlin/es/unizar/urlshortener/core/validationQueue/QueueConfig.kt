package es.unizar.urlshortener.core.blockingQueue

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.net.URL
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue

@Configuration
@EnableAsync
class QueueConfig {

    @Bean
    fun validationQueue(): BlockingQueue<String> = LinkedBlockingQueue()

    @Bean("validationExecutor")
    fun validationExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.setCorePoolSize(20)
        executor.setMaxPoolSize(20)
        executor.setQueueCapacity(1000)
        executor.setThreadNamePrefix("MyExecutor-")
        executor.initialize()
        return executor
    }
}