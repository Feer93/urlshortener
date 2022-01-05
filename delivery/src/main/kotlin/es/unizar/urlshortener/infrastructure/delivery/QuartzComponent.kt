package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.RecoverInfoUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.stereotype.Component

/**
 * Job used by the scheduler that updates the general stats
 * in the cache by recalculating them
 */
@Component
class GeneralStatsJob: Job {

    @Autowired
    private val info: RecoverInfoUseCase? = null

    /**
     * Method to execute to update the general stats
     * by executing every general stat handler available
     */
    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        info?.countRedirectionUpdate()
        info?.countURLUpdate()
        info?.recoverTopKRedirectionUpdate()
        info?.recoverTopKShortenedURLUpdate()
    }
}
