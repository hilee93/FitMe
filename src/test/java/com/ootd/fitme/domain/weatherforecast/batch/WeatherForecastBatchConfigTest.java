package com.ootd.fitme.domain.weatherforecast.batch;

import com.ootd.fitme.domain.weatherforecast.service.WeatherForecastCollectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class WeatherForecastBatchConfigTest {
    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private WeatherForecastCollectService collectService;

    @Test
    @DisplayName("weatherForecastCollectStep - 배치 Step 빈이 생성된다")
    void weatherForecastCollectStep_buildsStep() {
        WeatherForecastBatchConfig config = new WeatherForecastBatchConfig();

        Step step = config.weatherForecastCollectStep(jobRepository, transactionManager, collectService);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("weatherForecastCollectStep");
    }

    @Test
    @DisplayName("weatherForecastCollectJob - 배치 Job 빈이 생성된다")
    void weatherForecastCollectJob_buildsJob() {
        WeatherForecastBatchConfig config = new WeatherForecastBatchConfig();
        Step step = config.weatherForecastCollectStep(jobRepository, transactionManager, collectService);

        Job job = config.weatherForecastCollectJob(jobRepository, step);

        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("weatherForecastCollectJob");
    }
}
