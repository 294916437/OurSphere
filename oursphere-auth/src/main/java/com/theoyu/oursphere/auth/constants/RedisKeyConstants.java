package com.theoyu.oursphere.auth.constants;

public class RedisKeyConstants {
    /**
     * 验证码 KEY 前缀
     */
    private static final String VERIFICATION_CODE_KEY_PREFIX  = "verification_code:";

    /**
     * 构建验证码完整的 KEY
     * @param phone 手机号
     * @return string
     */
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }
}
