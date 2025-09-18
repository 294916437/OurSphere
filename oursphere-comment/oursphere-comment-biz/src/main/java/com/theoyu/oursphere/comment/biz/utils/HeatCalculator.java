package com.theoyu.oursphere.comment.biz.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HeatCalculator {
    // 热度计算的权重配置
    private static final double LIKE_WEIGHT = 0.6;  // 点赞权重 60%
    private static final double REPLY_WEIGHT = 0.4; // 回复权重 40%

    public static BigDecimal calculateHeat(long likeCount, long replyCount) {
        // 权重比例
        BigDecimal likeWeight = new BigDecimal(LIKE_WEIGHT);
        BigDecimal replyWeight = new BigDecimal(REPLY_WEIGHT);

        // 转换点赞数和回复数为 BigDecimal
        BigDecimal likeCountBD = new BigDecimal(likeCount);
        BigDecimal replyCountBD = new BigDecimal(replyCount);

        // 计算热度 (点赞数*点赞权重 + 回复数*回复权重)
        BigDecimal heat = likeCountBD.multiply(likeWeight).add(replyCountBD.multiply(replyWeight));

        // 四舍五入保留两位小数
        return heat.setScale(2, RoundingMode.HALF_UP);
    }
}
