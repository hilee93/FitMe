package com.ootd.fitme.global.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.StringQuery;

@Configuration
public class ElasticsearchHealthConfig {

    @Bean("elasticsearch")
    public HealthIndicator elasticsearchHealthIndicator(ElasticsearchOperations operations) {
        return () -> {
            try {
                operations.count(new StringQuery("{\"match_all\":{}}"), Object.class);

                return Health.up()
                        .withDetail("elasticsearch", "reachable")
                        .build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
}
