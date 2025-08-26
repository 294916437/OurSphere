package com.theoyu.oursphere.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.user.relation.biz.model.mapper")

public class OursphereUserRelationBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereUserRelationBizApplication.class, args);
    }

}
