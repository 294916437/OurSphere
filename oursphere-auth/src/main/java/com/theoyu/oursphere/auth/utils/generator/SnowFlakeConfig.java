package com.theoyu.oursphere.auth.config;

import com.theoyu.oursphere.auth.utils.generator.SnowFlake;
import com.theoyu.oursphere.auth.utils.generator.SnowFlakeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花算法配置类
 */
@Configuration
@EnableConfigurationProperties(SnowFlakeProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SnowFlakeConfig {

    private final SnowFlakeProperties snowFlakeProperties;

    /**
     * 创建雪花算法Bean
     */
    @Bean
    @ConditionalOnProperty(name = "snowflake.enabled", havingValue = "true", matchIfMissing = true)
    public SnowFlake snowFlake() {
        try {
            SnowFlake snowFlake = new SnowFlake(
                    snowFlakeProperties.getWorkerId(),
                    snowFlakeProperties.getDatacenterId()
            );

            log.info("SnowFlake Bean created successfully: {}", snowFlake.getConfig());
            return snowFlake;

        } catch (Exception e) {
            log.error("Failed to create SnowFlake Bean", e);
            throw new RuntimeException("SnowFlake initialization failed", e);
        }
    }
}