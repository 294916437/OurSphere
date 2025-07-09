package com.theoyu.framework.common.constants;

import java.time.format.DateTimeFormatter;

public class DateConstants {
    /**
     * DateTimeFormatter：年-月-日
     */
    public static final DateTimeFormatter DATE_FORMAT_Y_M_D = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * DateTimeFormatter：时：分：秒
     */
    public static final DateTimeFormatter DATE_FORMAT_H_M_S = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * DateTimeFormatter：年-月-日 时：分：秒
     */
    public static final DateTimeFormatter DATE_FORMAT_Y_M_D_H_M_S = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * DateTimeFormatter：年-月-日 时：分：秒.毫秒
     */
    public static final DateTimeFormatter DATE_FORMAT_Y_M_D_H_M_S_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * DateTimeFormatter：年-月
     */
    public static final DateTimeFormatter DATE_FORMAT_Y_M = DateTimeFormatter.ofPattern("yyyy-MM");

    // 保持向后兼容的String格式常量
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期时间格式（精确到毫秒）
     */
    public static final String DEFAULT_DATETIME_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
}