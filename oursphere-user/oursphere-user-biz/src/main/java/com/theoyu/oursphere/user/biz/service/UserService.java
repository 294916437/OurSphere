package com.theoyu.oursphere.user.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.user.biz.model.vo.UpdateUserInfoReqVO;

public interface UserService {
    /**
     * 更新用户信息
     *
     * @param updateUserInfoReqVO
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);
}
