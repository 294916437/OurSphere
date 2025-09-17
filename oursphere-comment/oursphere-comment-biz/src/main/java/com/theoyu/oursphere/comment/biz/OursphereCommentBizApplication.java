package com.theoyu.oursphere.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.comment.biz.model.mapper")
@EnableRetry
@EnableFeignClients(basePackages = { // 扫描OSS API，用于发现OpenFeign客户端
        "com.theoyu.oursphere.id.generator.api",
        "com.theoyu.oursphere.kv.api"
})
public class OursphereCommentBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereCommentBizApplication.class, args);
    }

}
