package com.theoyu.oursphere.auth.enums;

import com.theoyu.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("AUTH-10000", "系统错误，请稍后尝试..."),
    PARAM_NOT_VALID("AUTH-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    VERIFICATION_CODE_SEND_FREQUENTLY("AUTH-20000", "请求频繁，请于3分钟后再次尝试"),
    VERIFICATION_CODE_ERROR("AUTH-20001", "验证码错误"),

    // ----------- 用户登陆状态码 -----------
    LOGIN_TYPE_ERROR("AUTH-20002", "登录类型错误"),
    USER_NOT_FOUND("AUTH-20003", "该用户不存在"),
    PHONE_OR_PASSWORD_ERROR("AUTH-20004", "手机号或密码错误"),
    LOGIN_FAIL("AUTH-20005", "登录失败"),
    ;
    // 异常码，用于表示微服务和错误类型
    private final String errorCode;
    // 错误信息，展示错误的详细信息
    private final String errorMessage;



}


