package com.theoyu.oursphere.auth.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.auth.model.vo.user.UserLoginReqVO;
import com.theoyu.oursphere.auth.model.vo.user.UserPasswordReqVO;

public interface AuthService {

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
    /**
     * 修改密码
     * @param updatePasswordReqVO
     * @return
     */
    Response<?> updatePassword(UserPasswordReqVO updatePasswordReqVO);
}
