package com.theoyu.oursphere.oss.biz.factory;

import com.theoyu.oursphere.oss.biz.strategy.FileStrategy;
import com.theoyu.oursphere.oss.biz.strategy.impl.AliyunOSSFileStrategy;
import com.theoyu.oursphere.oss.biz.strategy.impl.MinioFileStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
public class FileStrategyFactory {


    @Value("${storage.type}")
    private String strategyType;

    @RefreshScope
    @Bean
    public FileStrategy getFileStrategy() {
        if (StringUtils.equals(strategyType, "minio")) {
            return new MinioFileStrategy();
        } else if (StringUtils.equals(strategyType, "aliyun")) {
            return new AliyunOSSFileStrategy();
        }
        throw new IllegalArgumentException("该存储类型不可用");
    }
}
