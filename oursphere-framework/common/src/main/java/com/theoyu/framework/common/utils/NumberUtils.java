package com.theoyu.framework.common.utils;

public class NumberUtils {
    //转化为中文字符串，0~9999正常显示，大于9999显示为xx.x万，大于1亿显示为xx.x亿
    public static String formatNumberString(Integer likeTotal) {
        if (likeTotal == null) {
            return "0";
        }
        if (likeTotal < 10000) {
            return String.valueOf(likeTotal);
        } else if (likeTotal < 100000000) {
            double result = likeTotal / 10000.0;
            return String.format("%.1f万", result);
        } else {
            double result = likeTotal / 100000000.0;
            return String.format("%.1f亿", result);
        }

    }
}
