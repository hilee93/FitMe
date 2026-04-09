package com.ootd.fitme.global.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaHealthConfig {

    @Bean("kafka")
    public HealthIndicator kafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        return () -> {
            try (AdminClient adminClient =
                         AdminClient.create(kafkaAdmin.getConfigurationProperties())) {

                var nodes = adminClient.describeCluster().nodes().get();

                return Health.up()
                        .withDetail("brokers", nodes.size())
                        .build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
}
