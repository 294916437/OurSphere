package com.theoyu.oursphere.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.user.relation.biz.model.mapper")
@EnableFeignClients(basePackages = { // 扫描OSS API，用于发现OpenFeign客户端
        "com.theoyu.oursphere.user.api",
})
public class OursphereUserRelationBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereUserRelationBizApplication.class, args);
    }

}
