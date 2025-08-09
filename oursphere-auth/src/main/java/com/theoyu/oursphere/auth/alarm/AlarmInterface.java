package com.theoyu.oursphere.auth.alarm;

public interface AlarmInterface {
    /**
     * 发送告警
     * @param content 告警内容
     */
    boolean sendAlarm(String content);

}
