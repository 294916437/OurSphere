package com.theoyu.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {

    // 正常状态
    ENABLE(0),
    // 禁用状态
    DISABLED(1);

    private final Integer value;
}
