package es.unizar.urlshortener

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import es.unizar.urlshortener.infrastructure.delivery.GeneralStatsJob
import org.quartz.*

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.context.ApplicationContext

import org.springframework.scheduling.quartz.SpringBeanJobFactory
import org.springframework.core.io.ClassPathResource

import org.springframework.scheduling.quartz.SchedulerFactoryBean

import org.quartz.JobDetail
import org.quartz.SimpleTrigger

import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean

import org.springframework.scheduling.quartz.JobDetailFactoryBean


/**
 * Quartz scheduler configuration
 */
@Configuration
@EnableScheduling
class QuartzScheduler(
    @Autowired val applicationContext: ApplicationContext
) {

    /**
     * Apply context and auto-wire support to the scheduler
     */
    @Bean
    fun springBeanJobFactory(): SpringBeanJobFactory {
        val jobFactory = AutoWiringSpringBeanJobFactory()
        jobFactory.setApplicationContext(applicationContext)
        return jobFactory
    }

    /**
     * Schedule a job with all of its components and using
     * a configuration file to establish some of the behaviour
     * of the scheduler
     */
    @Bean
    fun scheduler(trigger: Trigger, job: JobDetail): SchedulerFactoryBean {
        val schedulerFactory = SchedulerFactoryBean()
        schedulerFactory.setConfigLocation(
            ClassPathResource("quartz.properties"))
        schedulerFactory.setJobFactory(springBeanJobFactory())
        schedulerFactory.setJobDetails(job)
        schedulerFactory.setTriggers(trigger)
        return schedulerFactory
    }

    /**
     * Get the job and define some meta-data
     */
    @Bean
    fun jobDetail(): JobDetailFactoryBean {
        val jobDetailFactory = JobDetailFactoryBean()
        jobDetailFactory.setJobClass(GeneralStatsJob::class.java)
        jobDetailFactory.setName("GeneralStats_Job_Detail")
        jobDetailFactory.setDescription("Update general stats")
        jobDetailFactory.setDurability(true)
        return jobDetailFactory
    }

    /**
     * Establish when to execute a job
     */
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
