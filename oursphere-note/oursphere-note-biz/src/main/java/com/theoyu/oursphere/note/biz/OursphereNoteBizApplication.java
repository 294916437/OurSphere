package com.theoyu.oursphere.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.note.biz.model.mapper")
@EnableFeignClients(basePackages = { // 扫描OSS API，用于发现OpenFeign客户端
        "com.theoyu.oursphere.kv.api",
        "com.theoyu.oursphere.id.generator.api",
        "com.theoyu.oursphere.user.api",
})
public class OursphereNoteBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereNoteBizApplication.class, args);
    }

}
