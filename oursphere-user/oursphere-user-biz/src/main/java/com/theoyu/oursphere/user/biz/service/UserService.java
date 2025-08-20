package com.theoyu.oursphere.user.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.user.biz.model.vo.UpdateUserInfoReqVO;
import com.theoyu.oursphere.user.dto.request.FindUserByPhoneReqDTO;
import com.theoyu.oursphere.user.dto.request.RegisterUserReqDTO;
import com.theoyu.oursphere.user.dto.request.UpdateUserPasswordReqDTO;
import com.theoyu.oursphere.user.dto.response.FindUserByPhoneRspDTO;

public interface UserService {
    /**
     * 更新用户信息
     *
     * @param updateUserInfoReqVO
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);
    /**
     * 用户注册
     *
     * @param registerUserReqDTO
     * @return
     */
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);
    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneReqDTO
     * @return
     */
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);
    /**
     * 更新密码
     *
     * @param updateUserPasswordReqDTO
     * @return
     */
    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);
}
