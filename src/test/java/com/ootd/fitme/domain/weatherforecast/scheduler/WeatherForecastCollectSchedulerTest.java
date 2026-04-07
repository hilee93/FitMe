package com.ootd.fitme.domain.weatherforecast.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherForecastCollectSchedulerTest {
    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job weatherForecastCollectJob;

    private WeatherForecastCollectScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new WeatherForecastCollectScheduler(jobLauncher, weatherForecastCollectJob);
    }

    @Test
    @DisplayName("run - tirggerTime 파라미터를 넣어 jobLauncher.run을 호출")
    void run_callsJobLauncherWithTriggerTime() throws Exception {
        willReturn((JobExecution) null).given(jobLauncher).run(eq(weatherForecastCollectJob), any(JobParameters.class));

        scheduler.run();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        then(jobLauncher).should().run(eq(weatherForecastCollectJob), captor.capture());

        JobParameters params = captor.getValue();
        assertThat(params.getLong("triggerTime")).isNotNull();
        assertThat(params.getLong("triggerTime")).isPositive();
    }

    @Test
    @DisplayName("run - job 실행 중 예외가 나도 외부로 전파하지 않는다")
    void run_whenLauncherThrows_doesNotPropagate() throws Exception {
        willThrow(new RuntimeException("boom"))
                .given(jobLauncher).run(eq(weatherForecastCollectJob), any(JobParameters.class));

        assertThatCode(() -> scheduler.run()).doesNotThrowAnyException();
        then(jobLauncher).should().run(eq(weatherForecastCollectJob), any(JobParameters.class));
    }
}
