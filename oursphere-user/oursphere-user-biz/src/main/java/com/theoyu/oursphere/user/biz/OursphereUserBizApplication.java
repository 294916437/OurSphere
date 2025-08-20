package com.theoyu.oursphere.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.user.biz.model.mapper")
@EnableFeignClients(basePackages = {
        "com.theoyu.oursphere.oss.api",  // 扫描OSS API，用于发现OpenFeign客户端
})
public class OursphereUserBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereUserBizApplication.class, args);
    }

}
