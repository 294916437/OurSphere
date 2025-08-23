package com.theoyu.oursphere.id.generator.biz.core;

import com.theoyu.oursphere.id.generator.biz.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
