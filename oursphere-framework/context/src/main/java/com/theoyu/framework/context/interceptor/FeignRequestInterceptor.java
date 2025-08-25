package com.theoyu.framework.context.interceptor;

import com.theoyu.framework.common.constants.GlobalConstants;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public  class FeignRequestInterceptor  implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // 获取当前上下文中的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        // 若不为空，则添加到请求头中
        if (Objects.nonNull(userId)) {
            template.header(GlobalConstants.USER_ID, String.valueOf(userId));
        }
    }
}
