package com.theoyu.oursphere.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OursphereSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(OursphereSearchApplication.class, args);
    }

}
