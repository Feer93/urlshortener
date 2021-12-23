package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.RecoverInfoUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.stereotype.Component


@Component
class GeneralStatsJob: Job {

    @Autowired
    private val info: RecoverInfoUseCase? = null

    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        info?.countRedirectionUpdate()
        info?.countURLUpdate()
        info?.recoverTopKRedirectionUpdate()
        info?.recoverTopKShortenedURLUpdate()
    }
}
