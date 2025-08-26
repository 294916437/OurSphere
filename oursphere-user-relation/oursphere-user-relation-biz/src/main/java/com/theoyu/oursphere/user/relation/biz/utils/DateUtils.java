package com.theoyu.oursphere.user.relation.biz.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateUtils {
    /**
     * LocalDateTime 转时间戳
     *
     * @param localDateTime
     * @return
     */
    public static long localDateTime2Timestamp(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
