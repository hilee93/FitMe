package com.ootd.fitme.domain.weatherforecast.batch;

import com.ootd.fitme.domain.weatherforecast.service.WeatherForecastCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherForecastBatchConfig {
    @Bean
    public Job weatherForecastCollectJob(
            JobRepository jobRepository,
            Step weatherForecastCollectStep
    ) {
        return new JobBuilder("weatherForecastCollectJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(weatherForecastCollectStep)
                .build();
    }

    @Bean
    public Step weatherForecastCollectStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            WeatherForecastCollectService collectService
    ) {
        return new StepBuilder("weatherForecastCollectStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    collectService.collectAndStoreAllRegions();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
