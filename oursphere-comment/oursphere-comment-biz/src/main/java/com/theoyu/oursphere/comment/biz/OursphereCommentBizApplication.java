package com.theoyu.oursphere.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.theoyu.oursphere.comment.biz.model.mapper")
public class OursphereCommentBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereCommentBizApplication.class, args);
    }

}
