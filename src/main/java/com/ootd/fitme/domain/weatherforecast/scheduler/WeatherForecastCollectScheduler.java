package com.ootd.fitme.domain.weatherforecast.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherForecastCollectScheduler {
    private final JobLauncher jobLauncher;
    private final Job weatherForecastCollectJob;

    public WeatherForecastCollectScheduler(
            JobLauncher jobLauncher,
            @Qualifier("weatherForecastCollectJob")
            Job weatherForecastCollectJob
    ) {
        this.jobLauncher = jobLauncher;
        this.weatherForecastCollectJob = weatherForecastCollectJob;
    }

    @Scheduled(cron = "0 5 */3 * * *", zone = "Asia/Seoul")
    public void run() {
        JobParameters params = new JobParametersBuilder()
                .addLong("triggerTime", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(weatherForecastCollectJob, params);
        } catch (Exception e) {
            log.error("Weather forecast collect job failed", e);
        }
    }
}
