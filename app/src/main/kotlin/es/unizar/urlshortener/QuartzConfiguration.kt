package es.unizar.urlshortener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import es.unizar.urlshortener.infrastructure.delivery.GeneralStatsJob
import org.quartz.*

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationContext

import org.springframework.scheduling.quartz.SpringBeanJobFactory
import org.springframework.core.io.ClassPathResource

import org.springframework.scheduling.quartz.SchedulerFactoryBean

import org.quartz.JobDetail
import org.quartz.SimpleTrigger

import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean

import org.springframework.scheduling.quartz.JobDetailFactoryBean
import java.nio.file.Paths


/**
 * Quartz configuration
 */
@Configuration
//@EnableAutoConfiguration
@EnableScheduling
class QuartzScheduler(
    @Autowired val applicationContext: ApplicationContext
) {

    @Bean
    fun scheduler(trigger: Trigger, job: JobDetail): SchedulerFactoryBean {
        val schedulerFactory = SchedulerFactoryBean()
        val userDirectory: String = Paths.get("")
            .toAbsolutePath()
            .toString()
        schedulerFactory.setConfigLocation(
            ClassPathResource("quartz.properties"))
        val jobFactory = AutoWiringSpringBeanJobFactory()
        jobFactory.setApplicationContext(applicationContext)
        schedulerFactory.setJobFactory(jobFactory)
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        return schedulerFactory
    }

    @Bean
    fun jobDetail(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(GeneralStatsJob::class.java)
        jobDetailFactory.setName("GeneralStats_Job_Detail")
        jobDetailFactory.setDescription("Update general stats")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    @Bean
    fun trigger(job: JobDetail): SimpleTriggerFactoryBean {
        val trigger = SimpleTriggerFactoryBean()
        trigger.setJobDetail(job)
        val frequencyInSec = 1000
        trigger.setRepeatInterval((frequencyInSec * 60).toLong())
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
        trigger.setName("GeneralStats_Trigger")
        return trigger
    }

    /*
    @Bean
    fun jobDetail(): JobDetail = JobBuilder.newJob().ofType(GeneralStatsJob::class.java)
        .storeDurably()
        .withIdentity("GeneralStats_Job_Detail")
        .withDescription("Update general stats")
        .build()

    @Bean
    fun trigger(job: JobDetail): Trigger = TriggerBuilder.newTrigger().forJob(job)
        .withIdentity("GeneralStats_Trigger")
        .withDescription("Update general stats trigger")
        .withSchedule(simpleSchedule().repeatForever().withIntervalInMinutes(1))
        .build()
    */

}
