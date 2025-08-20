package com.theoyu.oursphere.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.user.biz.model.mapper")
public class OursphereUserBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereUserBizApplication.class, args);
    }

}
