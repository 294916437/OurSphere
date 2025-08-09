package com.theoyu.oursphere.auth.alarm.impl;

import com.theoyu.oursphere.auth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MailAlarmHelper implements AlarmInterface {
    /**
     * 发送告警信息
     * @param message
     * @return
     */
    @Override
    public boolean sendAlarm(String message) {
        log.info("==> 【邮件告警】：{}", message);

        // 业务逻辑...

        return true;
    }

}
