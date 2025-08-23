package com.theoyu.oursphere.note.biz.enums;

import com.theoyu.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {
    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("NOTE-10000", "系统错误，请稍后尝试..."),
    PARAM_NOT_VALID("NOTE-10001", "参数错误"),
    // ----------- 业务异常状态码 -----------
    MAX_FILE_SIZE_EXCEEDED("NOTE-20000", "文件大小超过限制，最大允许10MB"),
    ;


    // 异常码，用于表示微服务和错误类型
    private final String errorCode;
    // 错误信息，展示错误的详细信息
    private final String errorMessage;


}


