package com.theoyu.oursphere.auth;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.theoyu.oursphere.user.api")// 扫描USER API，用于发现OpenFeign客户端
@SpringBootApplication
public class OursphereAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereAuthApplication.class, args);
    }
}
