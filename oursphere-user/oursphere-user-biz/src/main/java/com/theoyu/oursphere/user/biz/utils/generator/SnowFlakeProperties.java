package com.theoyu.oursphere.user.biz.utils.generator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 雪花算法配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "snowflake")
public class SnowFlakeProperties {

    /**
     * 工作机器ID (0~31)
     */
    private Long workerId = 1L;

    /**
     * 数据中心ID (0~31)
     */
    private Long datacenterId = 1L;

    /**
     * 是否启用雪花算法
     */
    private Boolean enabled = true;
}