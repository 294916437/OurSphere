package com.theoyu.oursphere.auth.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.theoyu.oursphere.auth.alarm.AlarmInterface;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @NacosValue(value = "${rate-limit.api.limit}", autoRefreshed = true)
    private Integer limit;

    @GetMapping("/test")
    public String test() {
        return "当前限流阈值为: " + limit;
    }
    // 省略...

    @Resource
    private AlarmInterface alarm;


    @GetMapping("/alarm")
    public String sendAlarm() {
        alarm.sendAlarm("系统出错啦");
        return "alarm success";
    }
}
