package com.theoyu.oursphere.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.theoyu.oursphere.auth.domain.mapper")
@SpringBootApplication
public class OursphereAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereAuthApplication.class, args);
    }

}
