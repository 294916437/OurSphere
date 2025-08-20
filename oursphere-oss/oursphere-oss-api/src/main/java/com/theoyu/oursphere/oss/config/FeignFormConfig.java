package com.theoyu.oursphere.oss.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类，用于支持将对象编码为表单数据
 */
@Configuration
public class FeignFormConfig {
    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder();
    }

}
