package com.theoyu.oursphere.auth.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.auth.model.vo.user.UserLoginReqVO;

public interface UserService {

    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);
    /**
     * 退出登录
     * @return
     */
    Response<?> logout();
}
