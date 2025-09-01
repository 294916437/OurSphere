package com.theoyu.oursphere.count.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.count.biz.model.mapper")
public class OursphereCountBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereCountBizApplication.class, args);
    }

}
