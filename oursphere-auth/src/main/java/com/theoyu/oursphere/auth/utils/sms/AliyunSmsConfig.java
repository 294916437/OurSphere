package com.theoyu.oursphere.auth.utils.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AliyunSmsConfig {
    @Resource
    private AliyunAccessKeyProperties aliyunAccessKeyProperties;

    @Bean
    public Client smsClient() {
        try {
            Config config = new Config()
                    .setAccessKeyId(aliyunAccessKeyProperties.getAccessKeyId())
                    .setAccessKeySecret(aliyunAccessKeyProperties.getAccessKeySecret());

            // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
            config.endpoint = "dysmsapi.aliyuncs.com";

            return new Client(config);
        } catch (Exception e) {
            log.error("初始化阿里云短信服务错误: ", e);
            return null;
        }
    }

}
