package com.theoyu.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {

    // 正常状态
    ENABLE(1),
    // 禁用状态
    DISABLED(2);

    private final Integer value;
}
